package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
     * SDXL Inpainting 입력 데이터
     * <p>
     * 모델: lucataco/sdxl-inpainting (SDXL 기반)
     * - 원본 이미지 비율을 유지하면서 max 1024px 이내로 스케일링
     * - SD 2.0 대비 고해상도/고품질 결과물
     *
     * @see <a href="https://replicate.com/lucataco/sdxl-inpainting">Replicate 모델 페이지</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Input(
            String image,
            String mask,
            String prompt,
            @JsonProperty("negative_prompt")
            String negativePrompt,
            String scheduler,
            @JsonProperty("guidance_scale")
            Double guidanceScale,
            Integer steps,
            Double strength,
            @JsonProperty("num_outputs")
            Integer numOutputs,
            Integer seed
    ) {
        private static final String DEFAULT_PROMPT = "restore damaged photo, high quality, realistic, natural colors";
        private static final String DEFAULT_NEGATIVE_PROMPT = "monochrome, lowres, bad anatomy, worst quality, low quality";

        public static Input forRestoration(String imageUrl, String maskUrl) {
            return new Input(
                    imageUrl,
                    maskUrl,
                    DEFAULT_PROMPT,
                    DEFAULT_NEGATIVE_PROMPT,
                    "K_EULER",
                    8.0,
                    20,
                    0.7,
                    1,
                    null
            );
        }
    }
}
