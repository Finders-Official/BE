package com.finders.api.domain.community.converter;

import com.finders.api.domain.community.dto.response.CommentResponse;
import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.member.entity.Member;

import java.util.List;
import java.util.stream.Collectors;

public class CommentConverter {

    public static Comment toComment(String content, Post post, Member member) {
        return Comment.builder()
                .content(content)
                .post(post)
                .member(member)
                .build();
    }

    public static CommentResponse.CommentResDTO toCommentResDTO(Comment comment, Long currentMemberId) {
        return CommentResponse.CommentResDTO.builder()
                .commentId(comment.getId())
                .nickname(comment.getMember().getName())
                .profileImageUrl(comment.getMember().getProfileImage())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .isMine(comment.getMember().getId().equals(currentMemberId))
                .build();
    }

    public static CommentResponse.CommentListDTO toCommentListDTO(List<Comment> comments, Long currentMemberId) {
        List<CommentResponse.CommentResDTO> list = comments.stream()
                .map(comment -> toCommentResDTO(comment, currentMemberId))
                .collect(Collectors.toList());

        return CommentResponse.CommentListDTO.builder()
                .commentList(list)
                .listSize(list.size())
                .hasNext(false)
                .build();
    }
}