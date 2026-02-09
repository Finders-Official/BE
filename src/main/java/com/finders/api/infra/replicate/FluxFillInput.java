package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FluxFillInput(
        String prompt,
        String image,
        String mask,
        @JsonProperty("num_outputs")
        Integer numOutputs,
        @JsonProperty("num_inference_steps")
        Integer numInferenceSteps,
        Double guidance,
        String megapixels,
        @JsonProperty("output_format")
        String outputFormat,
        Integer seed
) implements ReplicateModelInput {

    private static final String MODEL_VERSION = "c96684b78bc3e49cf4c8e4bc03d3cea723a1942042b9d016ca6fbf3fd478c93e";
    private static final String DEFAULT_PROMPT = "restore damaged analog film photo, fix light leak and scratches, preserve film grain texture, maintain natural colors and vintage aesthetic";
    private static final Integer DEFAULT_NUM_OUTPUTS = 1;
    private static final Integer DEFAULT_NUM_INFERENCE_STEPS = 28;
    private static final Double DEFAULT_GUIDANCE = 30.0;
    private static final String DEFAULT_MEGAPIXELS = "match_input";
    private static final String DEFAULT_OUTPUT_FORMAT = "png";

    public static FluxFillInput forRestoration(String imageUrl, String maskUrl) {
        return FluxFillInput.builder()
                .prompt(DEFAULT_PROMPT)
                .image(imageUrl)
                .mask(maskUrl)
                .numOutputs(DEFAULT_NUM_OUTPUTS)
                .numInferenceSteps(DEFAULT_NUM_INFERENCE_STEPS)
                .guidance(DEFAULT_GUIDANCE)
                .megapixels(DEFAULT_MEGAPIXELS)
                .outputFormat(DEFAULT_OUTPUT_FORMAT)
                .build();
    }

    @Override
    public String modelVersion() {
        return MODEL_VERSION;
    }
}
