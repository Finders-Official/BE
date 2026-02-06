package com.finders.api.domain.photo.controller;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.infra.replicate.ReplicateClient;
import com.finders.api.infra.replicate.ReplicateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Replicate Test", description = "Replicate API 테스트용 (인증 불필요)")
@RestController
@RequestMapping("/replicate/test")
public class ReplicateTestController {

    private final ReplicateClient replicateClient;
    private final com.finders.api.infra.storage.StorageService storageService;

    public ReplicateTestController(
            ReplicateClient replicateClient,
            com.finders.api.infra.storage.StorageService storageService
    ) {
        this.replicateClient = replicateClient;
        this.storageService = storageService;
    }

    @Operation(summary = "Replicate Prediction 생성 테스트", description = "인증 없이 Replicate API를 통해 예측(Inpainting)을 생성합니다. GCS 경로나 HTTP URL을 모두 지원합니다.")
    @PostMapping("/predictions")
    public ApiResponse<ReplicateResponse.Prediction> createPrediction(
            @RequestParam String originalPath,
            @RequestParam String maskPath
    ) {
        String finalImageUrl = originalPath;
        String finalMaskUrl = maskPath;

        try {
            if (originalPath.startsWith("restorations/")) {
                finalImageUrl = storageService.getSignedUrl(originalPath, 60).url();
            }
            if (maskPath.startsWith("restorations/")) {
                finalMaskUrl = storageService.getSignedUrl(maskPath, 60).url();
            }
        } catch (Exception e) {
            throw new com.finders.api.global.exception.CustomException(
                com.finders.api.global.response.ErrorCode.INTERNAL_SERVER_ERROR,
                "GCS Signed URL 생성 실패 (로컬 구글 권한 확인 필요): " + e.getMessage()
            );
        }

        ReplicateResponse.Prediction response = replicateClient.createInpaintingPrediction(finalImageUrl, finalMaskUrl);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "Replicate Prediction 조회 테스트", description = "ID를 통해 Replicate API 예측 결과를 조회합니다.")
    @GetMapping("/predictions/{predictionId}")
    public ApiResponse<ReplicateResponse.Prediction> getPrediction(@PathVariable String predictionId) {
        ReplicateResponse.Prediction response = replicateClient.getPrediction(predictionId);
        return ApiResponse.ok(response);
    }
}
