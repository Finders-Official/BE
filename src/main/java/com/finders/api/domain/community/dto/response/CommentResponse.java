package com.finders.api.domain.community.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {

    @Builder
    public record CommentResDTO(
            Long commentId,
            String nickname,
            String profileImageUrl,
            String content,
            LocalDateTime createdAt,
            boolean isMine
    ) {}

    @Builder
    public record CommentListDTO(
            List<CommentResDTO> commentList,
            Integer listSize,
            boolean hasNext
    ) {}
}