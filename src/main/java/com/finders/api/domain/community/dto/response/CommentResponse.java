package com.finders.api.domain.community.dto.response;

import com.finders.api.domain.community.entity.Comment;
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
    ) {
        public static CommentResDTO from(Comment comment, Long currentMemberId) {
            return CommentResDTO.builder()
                    .commentId(comment.getId())
                    .nickname(comment.getMemberUser().getName())
                    .profileImageUrl(comment.getMemberUser().getProfileImage())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .isMine(comment.getMemberUser().getId().equals(currentMemberId))
                    .build();
        }
    }

    @Builder
    public record CommentListDTO(
            List<CommentResDTO> commentList,
            Integer listSize,
            boolean hasNext
    ) {
        public static CommentListDTO from(List<Comment> comments, Long currentMemberId) {
            return CommentListDTO.builder()
                    .commentList(comments.stream()
                            .map(comment -> CommentResDTO.from(comment, currentMemberId))
                            .toList())
                    .listSize(comments.size())
                    .hasNext(false)
                    .build();
        }
    }
}