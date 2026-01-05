package com.finders.api.domain.photo.service;

import com.finders.api.domain.member.service.TokenService;
import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.repository.PhotoRestorationRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.replicate.ReplicateClient;
import com.finders.api.infra.replicate.ReplicateResponse;
import com.finders.api.infra.storage.ByteArrayMultipartFile;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private static final String RELATED_TYPE = "PHOTO_RESTORATION";

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;
    private final TokenService tokenService;
    private final ReplicateClient replicateClient;

    @Transactional
    public RestorationResponse.Created createRestoration(Long memberId, RestorationRequest.Create request) {
        // 1. 토큰 잔액 확인 (차감은 복원 완료 시점에)
        if (!tokenService.hasEnoughTokens(memberId, RESTORATION_TOKEN_COST)) {
            throw new CustomException(ErrorCode.INSUFFICIENT_TOKEN);
        }

        // 2. 원본 이미지 업로드 (Private 버킷)
        StorageResponse.Upload originalUpload = storageService.uploadPrivate(
                request.originalImage(),
                StoragePath.RESTORATION_ORIGINAL,
                memberId
        );

        // 3. 마스크 이미지 업로드 (프론트에서 받은 그대로)
        StorageResponse.Upload maskUpload = storageService.uploadPrivate(
                request.maskImage(),
                StoragePath.RESTORATION_MASK,
                memberId
        );

        // 4. 복원 요청 저장 (토큰 차감은 완료 시점에)
        PhotoRestoration restoration = PhotoRestoration.builder()
                .memberId(memberId)
                .originalUrl(originalUpload.objectPath())
                .maskUrl(maskUpload.objectPath())
                .tokenUsed(RESTORATION_TOKEN_COST)
                .build();

        restoration = restorationRepository.save(restoration);

        // 5. Replicate API 호출
        String originalSignedUrl = storageService.getSignedUrl(originalUpload.objectPath(), SIGNED_URL_EXPIRY_MINUTES).url();
        String maskSignedUrl = storageService.getSignedUrl(maskUpload.objectPath(), SIGNED_URL_EXPIRY_MINUTES).url();

        ReplicateResponse.Prediction prediction = replicateClient.createInpaintingPrediction(
                originalSignedUrl,
                maskSignedUrl
        );

        // 6. 상태 업데이트
        restoration.startProcessing(prediction.id());

        int currentBalance = tokenService.getBalance(memberId);

        log.info("[PhotoRestoration] Created: id={}, predictionId={}, currentBalance={}",
                restoration.getId(), prediction.id(), currentBalance);

        return RestorationResponse.Created.builder()
                .id(restoration.getId())
                .status(restoration.getStatus())
                .tokenUsed(RESTORATION_TOKEN_COST)
                .remainingBalance(currentBalance)  // 아직 차감 전이므로 현재 잔액
                .build();
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

    public List<RestorationResponse.Summary> getRestorationHistory(Long memberId) {
        return restorationRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(restoration -> {
                    String thumbnailUrl = restoration.getRestoredUrl() != null
                            ? restoration.getRestoredUrl()
                            : restoration.getOriginalUrl();
                    String signedUrl = storageService.getSignedUrl(thumbnailUrl, SIGNED_URL_EXPIRY_MINUTES).url();
                    return RestorationResponse.Summary.from(restoration, signedUrl);
                })
                .toList();
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

        log.info("[PhotoRestoration] Feedback added: id={}, rating={}", restorationId, request.rating());
    }

    @Transactional
    public void completeRestoration(String predictionId, String restoredImageUrl) {
        PhotoRestoration restoration = restorationRepository.findByReplicatePredictionId(predictionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND,
                        "Prediction ID에 해당하는 복원 요청을 찾을 수 없습니다."));

        // 중복 webhook 방지: 이미 처리된 건 스킵
        if (restoration.isCompleted() || restoration.isFailed()) {
            log.warn("[PhotoRestoration] Already processed, skipping: id={}, status={}",
                    restoration.getId(), restoration.getStatus());
            return;
        }

        // 1. Replicate 결과 이미지 다운로드 및 GCS 업로드
        String restoredPath = downloadAndStoreResult(restoredImageUrl, restoration.getMemberId());

        // 2. 토큰 차감 (복원 성공 시점에 차감)
        tokenService.useTokens(
                restoration.getMemberId(),
                restoration.getTokenUsed(),
                RELATED_TYPE,
                restoration.getId(),
                "AI 사진 복원 완료"
        );

        // 3. 상태 업데이트
        restoration.complete(restoredPath);

        log.info("[PhotoRestoration] Completed: id={}, predictionId={}", restoration.getId(), predictionId);
    }

    @Transactional
    public void failRestoration(String predictionId, String errorMessage) {
        PhotoRestoration restoration = restorationRepository.findByReplicatePredictionId(predictionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND,
                        "Prediction ID에 해당하는 복원 요청을 찾을 수 없습니다."));

        // 중복 webhook 방지: 이미 처리된 건 스킵
        if (restoration.isCompleted() || restoration.isFailed()) {
            log.warn("[PhotoRestoration] Already processed, skipping: id={}, status={}",
                    restoration.getId(), restoration.getStatus());
            return;
        }

        restoration.fail(errorMessage);

        // 토큰은 복원 완료 시점에 차감하므로, 실패 시 환불 불필요
        log.info("[PhotoRestoration] Failed: id={}, predictionId={}, error={}",
                restoration.getId(), predictionId, errorMessage);
    }

    private PhotoRestoration getRestorationById(Long restorationId) {
        return restorationRepository.findById(restorationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));
    }

    private String downloadAndStoreResult(String resultUrl, Long memberId) {
        log.info("[PhotoRestoration] Downloading result image: url={}", resultUrl);

        try {
            // 1. Replicate 결과 이미지 다운로드
            byte[] imageBytes = WebClient.create()
                    .get()
                    .uri(resultUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (imageBytes == null || imageBytes.length == 0) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "복원 결과 이미지 다운로드 실패");
            }

            // 2. MultipartFile로 변환
            String filename = "restored.png";
            ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                    "file",
                    filename,
                    "image/png",
                    imageBytes
            );

            // 3. GCS에 업로드
            StorageResponse.Upload upload = storageService.uploadPrivate(
                    multipartFile,
                    StoragePath.RESTORATION_RESTORED,
                    memberId
            );

            log.info("[PhotoRestoration] Result image stored: path={}", upload.objectPath());
            return upload.objectPath();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PhotoRestoration] Failed to download/store result: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "복원 결과 이미지 저장 실패: " + e.getMessage());
        }
    }
}
