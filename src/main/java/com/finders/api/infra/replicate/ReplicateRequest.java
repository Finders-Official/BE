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
     * SDXL Inpainting 입력 데이터
     * <p>
     * 모델: lucataco/sdxl-inpainting (SDXL 기반)
     * - 원본 이미지 비율을 유지하면서 max 1024px 이내로 스케일링
     * - SD 2.0 대비 고해상도/고품질 결과물
     *
     * @see <a href="https://replicate.com/lucataco/sdxl-inpainting">Replicate 모델 페이지</a>
     */
    @Builder
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
        private static final String DEFAULT_PROMPT = "restore damaged analog film photo, fix light leak and scratches, preserve film grain texture, maintain natural colors and vintage aesthetic";
        private static final String DEFAULT_NEGATIVE_PROMPT = "monochrome, lowres, bad anatomy, worst quality, low quality";
        private static final String DEFAULT_SCHEDULER = "K_EULER";
        private static final Double DEFAULT_GUIDANCE_SCALE = 8.0;
        private static final Integer DEFAULT_STEPS = 20;
        private static final Double DEFAULT_STRENGTH = 0.7;
        private static final Integer DEFAULT_NUM_OUTPUTS = 1;

        public static Input forRestoration(String imageUrl, String maskUrl) {
            return Input.builder()
                    .image(imageUrl)
                    .mask(maskUrl)
                    .prompt(DEFAULT_PROMPT)
                    .negativePrompt(DEFAULT_NEGATIVE_PROMPT)
                    .scheduler(DEFAULT_SCHEDULER)
                    .guidanceScale(DEFAULT_GUIDANCE_SCALE)
                    .steps(DEFAULT_STEPS)
                    .strength(DEFAULT_STRENGTH)
                    .numOutputs(DEFAULT_NUM_OUTPUTS)
                    .build();
        }
    }
}
