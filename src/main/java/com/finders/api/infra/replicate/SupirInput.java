package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SupirInput(
        String image,
        Integer upscale,
        @JsonProperty("s_churn")
        Integer sChurn,
        @JsonProperty("s_noise")
        Integer sNoise,
        @JsonProperty("a_prompt")
        String aPrompt,
        @JsonProperty("n_prompt")
        String nPrompt,
        @JsonProperty("s_stage1")
        Double sStage1,
        @JsonProperty("s_stage2")
        Double sStage2,
        @JsonProperty("edm_steps")
        Integer edmSteps,
        @JsonProperty("color_fix_type")
        String colorFixType
) implements ReplicateModelInput {

    private static final String MODEL_VERSION = "cjwbw/supir-v0q:ede69f6a5ae7d09f769d683347325b08d2f83a93d136ed89747941205e0a71da";
    private static final Integer DEFAULT_UPSCALE = 2;
    private static final String DEFAULT_A_PROMPT = "high quality, detailed, restored analog film photograph, sharp focus, natural colors";
    private static final String DEFAULT_N_PROMPT = "blur, noise, artifacts, distortion, oversaturated, overprocessed";
    private static final Double DEFAULT_S_STAGE1 = 0.9;
    private static final Double DEFAULT_S_STAGE2 = 0.2;
    private static final Integer DEFAULT_EDM_STEPS = 50;
    private static final String DEFAULT_COLOR_FIX = "AdaIn";

    public static SupirInput forRestoration(String imageUrl) {
        return new SupirInput(
                imageUrl,
                DEFAULT_UPSCALE,
                null,
                null,
                DEFAULT_A_PROMPT,
                DEFAULT_N_PROMPT,
                DEFAULT_S_STAGE1,
                DEFAULT_S_STAGE2,
                DEFAULT_EDM_STEPS,
                DEFAULT_COLOR_FIX
        );
    }

    @Override
    public String modelVersion() {
        return MODEL_VERSION;
    }
}
