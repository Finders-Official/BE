package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.CommentResponse;
import com.finders.api.domain.member.entity.MemberUser;

public interface CommentQueryService {
    CommentResponse.CommentListDTO getCommentsByPost(Long postId, MemberUser memberUser);
}