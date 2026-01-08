package com.finders.api.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class PostRequest {
    // 게시글 작성
    @Builder
    public record CreatePostDTO(
            String title,
            String content,
            List<MultipartFile> images,
            boolean isSelfDeveloped,
            Long labId,
            String reviewContent
    ){}

    // 댓글 작성
    public record CreateCommentDTO(
            @NotBlank(message = "댓글 내용은 필수입니다.")
            String content
    ) {}
}
