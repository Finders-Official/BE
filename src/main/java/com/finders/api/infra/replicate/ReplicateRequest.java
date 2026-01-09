package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Replicate API 요청 DTO
 */
public class ReplicateRequest {

    /**
     * Prediction 생성 요청
     */
    public record CreatePrediction(
            String version,
            Input input,
            String webhook,
            @JsonProperty("webhook_events_filter")
            List<String> webhookEventsFilter
    ) {
        public static CreatePrediction of(String version, String imageUrl, String maskUrl, String webhookUrl) {
            return new CreatePrediction(
                    version,
                    Input.forRestoration(imageUrl, maskUrl),
                    webhookUrl,
                    List.of("completed")
            );
        }
    }

    /**
     * Inpainting 입력 데이터
     */
    public record Input(
            String image,
            String mask,
            String prompt,
            @JsonProperty("num_outputs")
            Integer numOutputs,
            @JsonProperty("guidance_scale")
            Double guidanceScale,
            @JsonProperty("num_inference_steps")
            Integer numInferenceSteps
    ) {
        private static final String DEFAULT_PROMPT = "restore damaged photo, high quality, realistic, natural colors";

        public static Input forRestoration(String imageUrl, String maskUrl) {
            return new Input(
                    imageUrl,
                    maskUrl,
                    DEFAULT_PROMPT,
                    1,
                    7.5,
                    50
            );
        }
    }
}
