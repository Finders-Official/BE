package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.dto.response.CommentResponse;

public interface CommentCommandService {
    CommentResponse.CommentResDTO createComment(Long postId, PostRequest.CreateCommentDTO request, Long memberId);
    void deleteComment(Long commentId, Long memberId);
}
