package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.CommentResponse;

public interface CommentQueryService {
    CommentResponse.CommentListDTO getCommentsByPost(Long postId, Long memberId);
}