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
        Integer sStage1,
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
    private static final Integer DEFAULT_S_STAGE1 = -1;
    private static final Double DEFAULT_S_STAGE2 = 0.2;
    private static final Integer DEFAULT_EDM_STEPS = 50;
    private static final String DEFAULT_COLOR_FIX = "AdaIn";

    public static SupirInput forRestoration(String imageUrl) {
        return SupirInput.builder()
                .image(imageUrl)
                .upscale(DEFAULT_UPSCALE)
                .aPrompt(DEFAULT_A_PROMPT)
                .nPrompt(DEFAULT_N_PROMPT)
                .sStage1(DEFAULT_S_STAGE1)
                .sStage2(DEFAULT_S_STAGE2)
                .edmSteps(DEFAULT_EDM_STEPS)
                .colorFixType(DEFAULT_COLOR_FIX)
                .build();
    }

    @Override
    public String modelVersion() {
        return MODEL_VERSION;
    }
}
