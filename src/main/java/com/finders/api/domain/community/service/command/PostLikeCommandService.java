package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.response.PostLikeResponse;

public interface PostLikeCommandService {

    PostLikeResponse.PostLikeResDTO createPostLike(Long postId, Long memberId);

    PostLikeResponse.PostLikeResDTO deletePostLike(Long postId, Long memberId);
}