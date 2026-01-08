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
            @Size(max = 200, message = "제목은 200자를 넘을 수 없습니다.")
            String title,

            @NotBlank(message = "내용은 필수입니다.")
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
