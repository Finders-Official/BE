package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.member.enums.CreditRelatedType;
import com.finders.api.domain.member.service.command.CreditCommandService;
import com.finders.api.domain.member.service.query.CreditQueryService;
import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.repository.PhotoRestorationRepository;
import com.finders.api.domain.photo.service.ImageMetadataService;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.domain.photo.enums.RestorationTier;
import com.finders.api.infra.replicate.KontextProInput;
import com.finders.api.infra.replicate.ReplicateClient;
import com.finders.api.infra.replicate.ReplicateResponse;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 사진 복원 Command 서비스 구현체
 */
@Service
@Transactional
public class PhotoRestorationCommandServiceImpl implements PhotoRestorationCommandService {

    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private static final Logger log = LoggerFactory.getLogger(PhotoRestorationCommandServiceImpl.class);

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;
    private final CreditQueryService creditQueryService;
    private final CreditCommandService creditCommandService;
    private final ReplicateClient replicateClient;
    private final ImageMetadataService imageMetadataService;

    private final WebClient longTimeoutWebClient;

    public PhotoRestorationCommandServiceImpl(
            PhotoRestorationRepository restorationRepository,
            StorageService storageService,
            CreditQueryService creditQueryService,
            CreditCommandService creditCommandService,
            ReplicateClient replicateClient,
            ImageMetadataService imageMetadataService,
            @Qualifier("longTimeoutWebClient") WebClient longTimeoutWebClient
    ) {
        this.restorationRepository = restorationRepository;
        this.storageService = storageService;
        this.creditQueryService = creditQueryService;
        this.creditCommandService = creditCommandService;
        this.replicateClient = replicateClient;
        this.imageMetadataService = imageMetadataService;
        this.longTimeoutWebClient = longTimeoutWebClient;
    }

    @Override
    public RestorationResponse.Created createRestoration(Long memberId, RestorationRequest.Create request) {
        RestorationTier tier = RestorationTier.BASIC;
        int creditCost = tier.getCreditCost();

        if (!creditQueryService.hasEnoughCredits(memberId, creditCost)) {
            throw new CustomException(ErrorCode.INSUFFICIENT_CREDIT);
        }

        validateRestorationPath(request.originalPath(), StoragePath.RESTORATION_ORIGINAL, memberId);

        if (request.maskPath() != null && !request.maskPath().isBlank()) {
            validateRestorationPath(request.maskPath(), StoragePath.RESTORATION_MASK, memberId);
        }

        PhotoRestoration restoration = PhotoRestoration.create(
                memberId,
                request.originalPath(),
                request.maskPath(),
                creditCost
        );

        restoration = restorationRepository.save(restoration);

        String originalSignedUrl = storageService.getSignedUrl(request.originalPath(), SIGNED_URL_EXPIRY_MINUTES).url();

        KontextProInput modelInput = KontextProInput.forRestoration(originalSignedUrl);
        ReplicateResponse.Prediction prediction = replicateClient.createPrediction(modelInput);

        restoration.startProcessing(prediction.id());

        int currentBalance = creditQueryService.getBalance(memberId);

        log.info("[PhotoRestorationCommandServiceImpl.createRestoration] Created: id={}, tier={}, predictionId={}, currentBalance={}",
                restoration.getId(), tier, prediction.id(), currentBalance);

        return new RestorationResponse.Created(
                restoration.getId(),
                restoration.getStatus(),
                creditCost,
                currentBalance
        );
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

        // 2. 크레딧 차감 (복원 성공 시점에 차감)
        creditCommandService.useCredits(
                restoration.getMemberId(),
                restoration.getCreditUsed(),
                CreditRelatedType.PHOTO_RESTORATION,
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

        // 크레딧은 복원 완료 시점에 차감하므로, 실패 시 환불 불필요
        log.info("[PhotoRestorationCommandServiceImpl.failRestoration] Failed: id={}, predictionId={}, error={}",
                restoration.getId(), predictionId, errorMessage);
    }

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

    private record RestoredImageResult(String objectPath, Integer width, Integer height) {
    }
}
