package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.member.enums.TokenRelatedType;
import com.finders.api.domain.member.service.TokenService;
import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.repository.PhotoRestorationRepository;
import com.finders.api.domain.photo.service.ImageMetadataService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 사진 복원 Command 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhotoRestorationCommandServiceImpl implements PhotoRestorationCommandService {

    private static final int RESTORATION_TOKEN_COST = 1;
    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;
    private final TokenService tokenService;
    private final ReplicateClient replicateClient;
    private final ImageMetadataService imageMetadataService;

    @Qualifier("longTimeoutWebClient")
    private final WebClient longTimeoutWebClient;

    @Override
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
                .originalPath(request.originalPath())
                .maskPath(request.maskPath())
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

        log.info("[PhotoRestorationCommandServiceImpl.createRestoration] Created: id={}, predictionId={}, currentBalance={}",
                restoration.getId(), prediction.id(), currentBalance);

        return RestorationResponse.Created.builder()
                .id(restoration.getId())
                .status(restoration.getStatus())
                .tokenUsed(RESTORATION_TOKEN_COST)
                .remainingBalance(currentBalance)  // 아직 차감 전이므로 현재 잔액
                .build();
    }

    @Override
    public void addFeedback(Long memberId, Long restorationId, RestorationRequest.Feedback request) {
        PhotoRestoration restoration = restorationRepository.findById(restorationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));

        if (!restoration.isOwner(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!restoration.isCompleted()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "완료된 복원에만 피드백을 남길 수 있습니다.");
        }

        restoration.addFeedback(request.rating(), request.comment());

        log.info("[PhotoRestorationCommandServiceImpl.addFeedback] Feedback added: id={}, rating={}", restorationId, request.rating());
    }

    @Override
    public void completeRestoration(String predictionId, String restoredImageUrl) {
        PhotoRestoration restoration = restorationRepository.findByReplicatePredictionId(predictionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND,
                        "Prediction ID에 해당하는 복원 요청을 찾을 수 없습니다."));

        // 중복 webhook 방지: 이미 처리된 건 스킵
        if (restoration.isCompleted() || restoration.isFailed()) {
            log.warn("[PhotoRestorationCommandServiceImpl.completeRestoration] Already processed, skipping: id={}, status={}",
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

        log.info("[PhotoRestorationCommandServiceImpl.completeRestoration] Completed: id={}, predictionId={}, size={}x{}",
                restoration.getId(), predictionId, result.width(), result.height());
    }

    @Override
    public void failRestoration(String predictionId, String errorMessage) {
        PhotoRestoration restoration = restorationRepository.findByReplicatePredictionId(predictionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND,
                        "Prediction ID에 해당하는 복원 요청을 찾을 수 없습니다."));

        // 중복 webhook 방지: 이미 처리된 건 스킵
        if (restoration.isCompleted() || restoration.isFailed()) {
            log.warn("[PhotoRestorationCommandServiceImpl.failRestoration] Already processed, skipping: id={}, status={}",
                    restoration.getId(), restoration.getStatus());
            return;
        }

        restoration.fail(errorMessage);

        // 토큰은 복원 완료 시점에 차감하므로, 실패 시 환불 불필요
        log.info("[PhotoRestorationCommandServiceImpl.failRestoration] Failed: id={}, predictionId={}, error={}",
                restoration.getId(), predictionId, errorMessage);
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

    /**
     * Replicate 결과 이미지를 다운로드하여 GCS에 저장하고 메타데이터 추출
     * <p>
     * 이 메서드는 백엔드에서 직접 업로드해야 하는 유일한 케이스입니다.
     * (webhook으로 받은 결과 URL → 다운로드 → 메타데이터 추출 → GCS 저장)
     */
    private RestoredImageResult downloadAndStoreResult(String resultUrl, Long memberId) {
        log.info("[PhotoRestorationCommandServiceImpl.downloadAndStoreResult] Downloading result image: url={}", resultUrl);

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

            log.info("[PhotoRestorationCommandServiceImpl.downloadAndStoreResult] Result image stored: path={}, size={}x{}",
                    upload.objectPath(), dimensions.width(), dimensions.height());

            return new RestoredImageResult(upload.objectPath(), dimensions.width(), dimensions.height());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PhotoRestorationCommandServiceImpl.downloadAndStoreResult] Failed to download/store result: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "복원 결과 이미지 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 복원된 이미지 결과 (경로 + 메타데이터)
     */
    private record RestoredImageResult(String objectPath, Integer width, Integer height) {
    }
}
