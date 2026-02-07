package com.finders.api.domain.community.dto.response;

import com.finders.api.domain.community.entity.Comment;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {

    @Builder
    public record CommentResDTO(
            Long commentId,
            Long postId,
            String nickname,
            String profileImageUrl,
            String content,
            LocalDateTime createdAt,
            boolean isMine
    ) {
        public static CommentResDTO from(Comment comment, Long currentMemberId, String profileImageUrl) {
            return CommentResDTO.builder()
                    .commentId(comment.getId())
                    .postId(comment.getPost().getId())
                    .nickname(comment.getMemberUser().getName())
                    .profileImageUrl(profileImageUrl)
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
        public static CommentListDTO from(List<CommentResDTO> commentResDTO, boolean hasNext) {
            return CommentListDTO.builder()
                    .commentList(commentResDTO)
                    .listSize(commentResDTO.size())
                    .hasNext(hasNext)
                    .build();
        }
    }
}