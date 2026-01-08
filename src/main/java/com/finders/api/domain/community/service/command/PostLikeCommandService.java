package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.response.PostLikeResponse;
import com.finders.api.domain.member.entity.Member;

public interface PostLikeCommandService {

    PostLikeResponse.PostLikeResDTO createPostLike(Long postId, Member member);

    PostLikeResponse.PostLikeResDTO deletePostLike(Long postId, Member member);
}