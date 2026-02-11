package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KontextProInput(
        String prompt,
        @JsonProperty("input_image")
        String inputImage,
        @JsonProperty("output_format")
        String outputFormat,
        @JsonProperty("aspect_ratio")
        String aspectRatio,
        @JsonProperty("safety_tolerance")
        Integer safetyTolerance,
        Integer seed
) implements ReplicateModelInput {

    private static final String MODEL_ID = "black-forest-labs/flux-kontext-pro";
    private static final String DEFAULT_PROMPT = "Restore this damaged analog film photograph. "
            + "Remove all bright white light leaks, overexposed areas, and film burn damage. "
            + "Reconstruct the missing scene content that was destroyed by the light leak, "
            + "matching the style, colors, and composition of the visible parts. "
            + "Preserve the original film aesthetic and grain.";
    private static final String DEFAULT_OUTPUT_FORMAT = "png";
    private static final String DEFAULT_ASPECT_RATIO = "match_input_image";
    private static final Integer DEFAULT_SAFETY_TOLERANCE = 2;

    public static KontextProInput forRestoration(String imageUrl) {
        return new KontextProInput(
                DEFAULT_PROMPT,
                imageUrl,
                DEFAULT_OUTPUT_FORMAT,
                DEFAULT_ASPECT_RATIO,
                DEFAULT_SAFETY_TOLERANCE,
                null
        );
    }

    @Override
    public String modelVersion() {
        return MODEL_ID;
    }

    @Override
    public boolean isOfficialModel() {
        return true;
    }
}
