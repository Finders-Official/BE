package com.finders.api.domain.photo.dto;

import com.finders.api.domain.photo.enums.FeedbackRating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 복원 요청 DTO
 */
public class RestorationRequest {

    /**
     * 복원 생성 요청
     * <p>
     * 프론트에서 Presigned URL로 GCS에 직접 업로드한 후, objectPath만 전달합니다.
     * 예: "restorations/123/original/uuid.png"
     */
    public record Create(
            @NotBlank(message = "원본 이미지 경로는 필수입니다.")
            String originalPath,

            @NotBlank(message = "마스크 이미지 경로는 필수입니다.")
            String maskPath
    ) {}

    /**
     * 피드백 요청
     */
    public record Feedback(
            @NotNull(message = "평가는 필수입니다.")
            FeedbackRating rating,

            @Size(max = 500, message = "코멘트는 500자 이내로 작성해주세요.")
            String comment
    ) {}
}
