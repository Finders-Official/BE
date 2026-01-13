package com.finders.api.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class PostRequest {
    // 게시글 작성
    @Builder
    public record CreatePostDTO(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(min = 2, max = 30, message = "제목은 최소 2자, 최대 30자 이내여야 합니다.")
            String title,

            @NotBlank(message = "내용은 필수입니다.")
            @Size(min = 20, max = 300, message = "본문은 최소 20자, 최대 300자 이내여야 합니다.")
            String content,

            @Size(max = 10, message = "사진은 최대 10장가지 가능합니다.")
            List<MultipartFile> images,

            boolean isSelfDeveloped,
            Long labId,

            @Size(min = 20, max = 300, message = "리뷰는 최소 20자, 최대 300자 이내여야 합니다.")
            String reviewContent
    ){}

    // 댓글 작성
    public record CreateCommentDTO(
            @NotBlank(message = "댓글 내용은 필수입니다.")
            String content
    ) {}
}
