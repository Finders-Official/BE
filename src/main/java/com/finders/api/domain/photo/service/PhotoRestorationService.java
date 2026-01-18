package com.finders.api.domain.photo.service;

import com.finders.api.domain.member.enums.TokenRelatedType;
import com.finders.api.domain.member.service.TokenService;
import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.repository.PhotoRestorationRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.replicate.ReplicateClient;
import com.finders.api.infra.replicate.ReplicateResponse;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 사진 복원 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoRestorationService {

    private static final int RESTORATION_TOKEN_COST = 1;
    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;
    private final TokenService tokenService;
    private final ReplicateClient replicateClient;
    private final ImageMetadataService imageMetadataService;

    @Qualifier("longTimeoutWebClient")
    private final WebClient longTimeoutWebClient;

    /**
     * 복원 요청 생성
     * <p>
     * 프론트에서 Presigned URL로 GCS에 직접 업로드한 후, objectPath만 전달받습니다.
     *
     * @param memberId 회원 ID
     * @param request  복원 요청 (originalPath, maskPath)
     * @return 생성된 복원 정보
     */
    @Transactional
    public RestorationResponse.Created createRestoration(Long memberId, RestorationRequest.Create request) {
        // 1. 토큰 잔액 확인 (차감은 복원 완료 시점에)
        if (!tokenService.hasEnoughTokens(memberId, RESTORATION_TOKEN_COST)) {
            throw new CustomException(ErrorCode.INSUFFICIENT_TOKEN);
        }

        // 2. 경로 검증 (프론트에서 전달받은 경로가 유효한지)
        validateRestorationPath(request.originalPath(), StoragePath.RESTORATION_ORIGINAL, memberId);
        validateRestorationPath(request.maskPath(), StoragePath.RESTORATION_MASK, memberId);

        // 3. 복원 요청 저장 (토큰 차감은 완료 시점에)
        PhotoRestoration restoration = PhotoRestoration.builder()
                .memberId(memberId)
                .originalUrl(request.originalPath())
                .maskUrl(request.maskPath())
                .tokenUsed(RESTORATION_TOKEN_COST)
                .build();

        restoration = restorationRepository.save(restoration);

        // 4. Replicate API 호출을 위한 Signed URL 생성
        String originalSignedUrl = storageService.getSignedUrl(request.originalPath(), SIGNED_URL_EXPIRY_MINUTES).url();
        String maskSignedUrl = storageService.getSignedUrl(request.maskPath(), SIGNED_URL_EXPIRY_MINUTES).url();

        ReplicateResponse.Prediction prediction = replicateClient.createInpaintingPrediction(
                originalSignedUrl,
                maskSignedUrl
        );

        // 5. 상태 업데이트
        restoration.startProcessing(prediction.id());

        int currentBalance = tokenService.getBalance(memberId);

        log.info("[PhotoRestorationService.createRestoration] Created: id={}, predictionId={}, currentBalance={}",
                restoration.getId(), prediction.id(), currentBalance);

        return RestorationResponse.Created.builder()
                .id(restoration.getId())
                .status(restoration.getStatus())
                .tokenUsed(RESTORATION_TOKEN_COST)
                .remainingBalance(currentBalance)  // 아직 차감 전이므로 현재 잔액
                .build();
    }

    /**
     * 복원 경로 검증
     * - 경로 형식이 올바른지
     * - 본인의 memberId로 된 경로인지
     */
    private void validateRestorationPath(String objectPath, StoragePath expectedType, Long memberId) {
        if (objectPath == null || objectPath.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지 경로가 비어있습니다.");
        }

        StoragePath actualType = StoragePath.fromObjectPath(objectPath);
        if (actualType != expectedType) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 이미지 경로입니다: " + objectPath);
        }

        Long pathMemberId = actualType.extractId(objectPath);
        if (!memberId.equals(pathMemberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인의 이미지만 사용할 수 있습니다.");
        }
    }

    public RestorationResponse.Detail getRestoration(Long memberId, Long restorationId) {
        PhotoRestoration restoration = getRestorationById(restorationId);

        if (!restoration.isOwner(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String originalSignedUrl = storageService.getSignedUrl(
                restoration.getOriginalUrl(), SIGNED_URL_EXPIRY_MINUTES).url();

        String restoredSignedUrl = null;
        if (restoration.getRestoredUrl() != null) {
            restoredSignedUrl = storageService.getSignedUrl(
                    restoration.getRestoredUrl(), SIGNED_URL_EXPIRY_MINUTES).url();
        }

        return RestorationResponse.Detail.from(restoration, originalSignedUrl, restoredSignedUrl);
    }

    /**
     * 복원 이력 조회 (페이지네이션 + 배치 Signed URL 생성)
     * <p>
     * N+1 쿼리 최적화: 배치로 Signed URL을 생성하여 GCS API 호출 횟수를 줄입니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이지 정보 (page, size, sort)
     * @return 복원 이력 페이지
     */
    public Page<RestorationResponse.Summary> getRestorationHistory(Long memberId, Pageable pageable) {
        Page<PhotoRestoration> restorations = restorationRepository.findByMemberId(memberId, pageable);

        // 1. 모든 썸네일 경로 수집
        List<String> thumbnailPaths = restorations.getContent().stream()
                .map(restoration -> restoration.getRestoredUrl() != null
                        ? restoration.getRestoredUrl()
                        : restoration.getOriginalUrl())
                .toList();

        // 2. 배치로 Signed URL 생성 (N+1 쿼리 방지)
        Map<String, StorageResponse.SignedUrl> signedUrlMap = storageService.getSignedUrls(
                thumbnailPaths,
                SIGNED_URL_EXPIRY_MINUTES
        );

        // 3. Response 변환
        return restorations.map(restoration -> {
            String thumbnailPath = restoration.getRestoredUrl() != null
                    ? restoration.getRestoredUrl()
                    : restoration.getOriginalUrl();
            String signedUrl = signedUrlMap.get(thumbnailPath).url();
            return RestorationResponse.Summary.from(restoration, signedUrl);
        });
    }

    @Transactional
    public void addFeedback(Long memberId, Long restorationId, RestorationRequest.Feedback request) {
        PhotoRestoration restoration = getRestorationById(restorationId);

        if (!restoration.isOwner(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!restoration.isCompleted()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "완료된 복원에만 피드백을 남길 수 있습니다.");
        }

        restoration.addFeedback(request.rating(), request.comment());

        log.info("[PhotoRestorationService.addFeedback] Feedback added: id={}, rating={}", restorationId, request.rating());
    }

    @Transactional
    public void completeRestoration(String predictionId, String restoredImageUrl) {
        PhotoRestoration restoration = restorationRepository.findByReplicatePredictionId(predictionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND,
                        "Prediction ID에 해당하는 복원 요청을 찾을 수 없습니다."));

        // 중복 webhook 방지: 이미 처리된 건 스킵
        if (restoration.isCompleted() || restoration.isFailed()) {
            log.warn("[PhotoRestorationService.completeRestoration] Already processed, skipping: id={}, status={}",
                    restoration.getId(), restoration.getStatus());
            return;
        }

        // 1. Replicate 결과 이미지 다운로드 및 GCS 업로드 (메타데이터 포함)
        RestoredImageResult result = downloadAndStoreResult(restoredImageUrl, restoration.getMemberId());

        // 2. 토큰 차감 (복원 성공 시점에 차감)
        tokenService.useTokens(
                restoration.getMemberId(),
                restoration.getTokenUsed(),
                TokenRelatedType.PHOTO_RESTORATION,
                restoration.getId(),
                "AI 사진 복원 완료"
        );

        // 3. 상태 업데이트 (메타데이터 포함)
        restoration.complete(result.objectPath(), result.width(), result.height());

        log.info("[PhotoRestorationService.completeRestoration] Completed: id={}, predictionId={}, size={}x{}",
                restoration.getId(), predictionId, result.width(), result.height());
    }

    @Transactional
    public void failRestoration(String predictionId, String errorMessage) {
        PhotoRestoration restoration = restorationRepository.findByReplicatePredictionId(predictionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND,
                        "Prediction ID에 해당하는 복원 요청을 찾을 수 없습니다."));

        // 중복 webhook 방지: 이미 처리된 건 스킵
        if (restoration.isCompleted() || restoration.isFailed()) {
            log.warn("[PhotoRestorationService.failRestoration] Already processed, skipping: id={}, status={}",
                    restoration.getId(), restoration.getStatus());
            return;
        }

        restoration.fail(errorMessage);

        // 토큰은 복원 완료 시점에 차감하므로, 실패 시 환불 불필요
        log.info("[PhotoRestorationService.failRestoration] Failed: id={}, predictionId={}, error={}",
                restoration.getId(), predictionId, errorMessage);
    }

    private PhotoRestoration getRestorationById(Long restorationId) {
        return restorationRepository.findById(restorationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));
    }

    /**
     * Replicate 결과 이미지를 다운로드하여 GCS에 저장하고 메타데이터 추출
     * <p>
     * 이 메서드는 백엔드에서 직접 업로드해야 하는 유일한 케이스입니다.
     * (webhook으로 받은 결과 URL → 다운로드 → 메타데이터 추출 → GCS 저장)
     */
    private RestoredImageResult downloadAndStoreResult(String resultUrl, Long memberId) {
        log.info("[PhotoRestorationService.downloadAndStoreResult] Downloading result image: url={}", resultUrl);

        try {
            // 1. Replicate 결과 이미지 다운로드 (120초 타임아웃)
            byte[] imageBytes = longTimeoutWebClient.get()
                    .uri(resultUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (imageBytes == null || imageBytes.length == 0) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "복원 결과 이미지 다운로드 실패");
            }

            // 2. 메타데이터 추출 (width, height)
            ImageMetadataService.ImageDimensions dimensions = imageMetadataService.extractDimensions(imageBytes);

            // 3. GCS에 직접 업로드 (byte[] 사용)
            StorageResponse.Upload upload = storageService.uploadBytes(
                    imageBytes,
                    "image/png",
                    StoragePath.RESTORATION_RESTORED,
                    memberId,
                    "restored.png"
            );

            log.info("[PhotoRestorationService.downloadAndStoreResult] Result image stored: path={}, size={}x{}",
                    upload.objectPath(), dimensions.width(), dimensions.height());

            return new RestoredImageResult(upload.objectPath(), dimensions.width(), dimensions.height());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PhotoRestorationService.downloadAndStoreResult] Failed to download/store result: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "복원 결과 이미지 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 복원된 이미지 결과 (경로 + 메타데이터)
     */
    private record RestoredImageResult(String objectPath, Integer width, Integer height) {
    }
}
