package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;

public interface CommentCommandService {
    Long createComment(Long postId, PostRequest.CreateCommentDTO request, Long memberId);
    void deleteComment(Long commentId, Long memberId);
}
