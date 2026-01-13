package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.response.PostLikeResponse;
import com.finders.api.domain.member.entity.MemberUser;

public interface PostLikeCommandService {

    PostLikeResponse.PostLikeResDTO createPostLike(Long postId, MemberUser memberUser);

    PostLikeResponse.PostLikeResDTO deletePostLike(Long postId, MemberUser memberUser);
}