package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.member.entity.MemberUser;

public interface CommentCommandService {
    Long createComment(Long postId, PostRequest.CreateCommentDTO request, MemberUser memberUser);
    void deleteComment(Long commentId, MemberUser memberUser);
}
