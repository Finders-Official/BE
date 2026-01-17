package com.finders.api.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class InquiryRequest {

    /**
     * 문의 생성 요청
     */
    public record CreateInquiryDTO(
            Long photoLabId,

            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
            String title,

            @NotBlank(message = "문의 내용은 필수입니다.")
            @Size(max = 500, message = "문의 내용은 500자 이내여야 합니다.")
            String content,

            @Size(max = 5, message = "이미지는 최대 5개까지 첨부할 수 있습니다.")
            List<String> imageUrls
    ) {}

    /**
     * 문의 답변 작성 요청
     */
    public record CreateReplyDTO(
            @NotBlank(message = "답변 내용은 필수입니다.")
            @Size(max = 2000, message = "답변 내용은 2000자 이내여야 합니다.")
            String content
    ) {}
}
