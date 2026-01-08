package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.member.entity.Member;

public interface CommentCommandService {
    Long createComment(Long postId, PostRequest.CreateCommentDTO request, Member member);
    void deleteComment(Long CommentId, Member member);
}
