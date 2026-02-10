package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestoreImageInput(
        @JsonProperty("input_image")
        String inputImage,
        @JsonProperty("output_format")
        String outputFormat,
        @JsonProperty("safety_tolerance")
        Integer safetyTolerance,
        Integer seed
) implements ReplicateModelInput {

    // flux-kontext-apps/restore-image — uses model-based endpoint, not version-based
    private static final String MODEL_VERSION = "flux-kontext-apps/restore-image";
    private static final String DEFAULT_OUTPUT_FORMAT = "png";
    private static final Integer DEFAULT_SAFETY_TOLERANCE = 2;

    public static RestoreImageInput forRestoration(String imageUrl) {
        return new RestoreImageInput(
                imageUrl,
                DEFAULT_OUTPUT_FORMAT,
                DEFAULT_SAFETY_TOLERANCE,
                null
        );
    }

    @Override
    public String modelVersion() {
        return MODEL_VERSION;
    }
}
