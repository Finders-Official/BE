package com.finders.api.infra.replicate;

// TODO: flux-kontext-apps/restore-image 모델 지원 시 활성화
//       Official model은 /v1/models/{owner}/{name}/predictions 엔드포인트 사용 필요
//
// import com.fasterxml.jackson.annotation.JsonInclude;
// import com.fasterxml.jackson.annotation.JsonProperty;
// import lombok.Builder;
//
// @Builder
// @JsonInclude(JsonInclude.Include.NON_NULL)
// public record RestoreImageInput(
//         @JsonProperty("input_image")
//         String inputImage,
//         @JsonProperty("output_format")
//         String outputFormat,
//         @JsonProperty("safety_tolerance")
//         Integer safetyTolerance,
//         Integer seed
// ) implements ReplicateModelInput {
//
//     private static final String MODEL_VERSION = "flux-kontext-apps/restore-image";
//     private static final String DEFAULT_OUTPUT_FORMAT = "png";
//     private static final Integer DEFAULT_SAFETY_TOLERANCE = 2;
//
//     public static RestoreImageInput forRestoration(String imageUrl) {
//         return new RestoreImageInput(
//                 imageUrl,
//                 DEFAULT_OUTPUT_FORMAT,
//                 DEFAULT_SAFETY_TOLERANCE,
//                 null
//         );
//     }
//
//     @Override
//     public String modelVersion() {
//         return MODEL_VERSION;
//     }
// }
