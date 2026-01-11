package com.finders.api.domain.photo.dto;

import com.finders.api.domain.photo.enums.FeedbackRating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * 복원 요청 DTO
 */
public class RestorationRequest {

    /**
     * 복원 생성 요청
     */
    public record Create(
            @NotNull(message = "원본 이미지는 필수입니다.")
            MultipartFile originalImage,

            @NotNull(message = "마스크 이미지는 필수입니다.")
            MultipartFile maskImage
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
