package com.finders.api.domain.photo.controller;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.infra.replicate.ReplicateClient;
import com.finders.api.infra.replicate.ReplicateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
            @RequestParam String imageUrl,
            @RequestParam String maskUrl
    ) {
        String finalImageUrl = imageUrl.startsWith("restorations/") 
                ? storageService.getSignedUrl(imageUrl, 60).url() 
                : imageUrl;
        String finalMaskUrl = maskUrl.startsWith("restorations/") 
                ? storageService.getSignedUrl(maskUrl, 60).url() 
                : maskUrl;

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
