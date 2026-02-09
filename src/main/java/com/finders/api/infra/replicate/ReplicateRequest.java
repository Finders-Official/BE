package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Replicate API 요청 DTO
 */
public class ReplicateRequest {

    /**
     * Prediction 생성 요청
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreatePrediction(
            String version,
            Input input,
            String webhook,
            @JsonProperty("webhook_events_filter")
            List<String> webhookEventsFilter
    ) {
        public static CreatePrediction of(String version, String imageUrl, String maskUrl, String webhookUrl) {
            String finalWebhook = (webhookUrl != null && webhookUrl.startsWith("https://")) ? webhookUrl : null;
            
            return new CreatePrediction(
                    version,
                    Input.forRestoration(imageUrl, maskUrl),
                    finalWebhook,
                    finalWebhook != null ? List.of("completed") : null
            );
        }
    }

    /**
     * FLUX.1 Kontext Pro 입력 데이터
     * <p>
     * 모델: black-forest-labs/flux-kontext-pro
     * - 원본 이미지 비율 유지 (aspect_ratio: match_input_image)
     * - 필름 사진 복원 특화
     *
     * @see <a href="https://replicate.com/black-forest-labs/flux-kontext-pro">Replicate 모델 페이지</a>
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Input(
            @JsonProperty("input_image")
            String inputImage,
            String prompt,
            @JsonProperty("aspect_ratio")
            String aspectRatio,
            @JsonProperty("output_format")
            String outputFormat,
            @JsonProperty("prompt_upsampling")
            Boolean promptUpsampling,
            @JsonProperty("safety_tolerance")
            Integer safetyTolerance,
            Integer seed
    ) {
        private static final String DEFAULT_PROMPT = "restore damaged analog film photo, fix light leak and scratches, preserve film grain texture, maintain natural colors and vintage aesthetic";
        private static final String DEFAULT_ASPECT_RATIO = "match_input_image";
        private static final String DEFAULT_OUTPUT_FORMAT = "png";
        private static final Boolean DEFAULT_PROMPT_UPSAMPLING = false;
        private static final Integer DEFAULT_SAFETY_TOLERANCE = 2;

        public static Input forRestoration(String imageUrl, String maskUrl) {
            return Input.builder()
                    .inputImage(imageUrl)
                    .prompt(DEFAULT_PROMPT)
                    .aspectRatio(DEFAULT_ASPECT_RATIO)
                    .outputFormat(DEFAULT_OUTPUT_FORMAT)
                    .promptUpsampling(DEFAULT_PROMPT_UPSAMPLING)
                    .safetyTolerance(DEFAULT_SAFETY_TOLERANCE)
                    .build();
        }
    }
}
