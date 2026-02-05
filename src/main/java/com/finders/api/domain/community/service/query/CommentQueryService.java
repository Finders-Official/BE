package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.CommentResponse;
import org.springframework.data.domain.Page;

public interface CommentQueryService {
    Page<CommentResponse.CommentResDTO> getCommentsByPost(Long postId, Long memberId, Integer page, Integer size);
}