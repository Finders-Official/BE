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
                    .nickname(comment.getMember().getName())
                    .profileImageUrl(comment.getMember().getProfileImage())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .isMine(comment.getMember().getId().equals(currentMemberId))
                    .build();
        }
    }

    @Builder
    public record CommentListDTO(
            List<CommentResDTO> commentList,
            Integer listSize,
            boolean hasNext
    ) {
        // 💡 리스트를 변환하는 메서드 (이름 수정)
        public static CommentListDTO from(List<Comment> comments, Long currentMemberId) {
            return CommentListDTO.builder()
                    .commentList(comments.stream()
                            .map(comment -> CommentResDTO.from(comment, currentMemberId)) // 💡 여기서 CommentResDTO를 호출!
                            .toList())
                    .listSize(comments.size())
                    .hasNext(false)
                    .build();
        }
    }
}