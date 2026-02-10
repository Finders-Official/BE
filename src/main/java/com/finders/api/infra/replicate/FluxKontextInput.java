package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FluxKontextInput(
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
) implements ReplicateModelInput {

    private static final String MODEL_VERSION = "3b04064a22c483113223a8e15f5e7e9e23c2e37ac02ef182a4a4c09f49a7f0d9";
    private static final String DEFAULT_PROMPT = "restore damaged analog film photo, fix light leak and scratches, preserve film grain texture, maintain natural colors and vintage aesthetic";
    private static final String DEFAULT_ASPECT_RATIO = "match_input_image";
    private static final String DEFAULT_OUTPUT_FORMAT = "png";
    private static final Boolean DEFAULT_PROMPT_UPSAMPLING = false;
    private static final Integer DEFAULT_SAFETY_TOLERANCE = 2;

    public static FluxKontextInput forRestoration(String imageUrl, String maskUrl) {
        return FluxKontextInput.builder()
                .inputImage(imageUrl)
                .prompt(DEFAULT_PROMPT)
                .aspectRatio(DEFAULT_ASPECT_RATIO)
                .outputFormat(DEFAULT_OUTPUT_FORMAT)
                .promptUpsampling(DEFAULT_PROMPT_UPSAMPLING)
                .safetyTolerance(DEFAULT_SAFETY_TOLERANCE)
                .build();
    }

    @Override
    public String modelVersion() {
        return MODEL_VERSION;
    }
}
