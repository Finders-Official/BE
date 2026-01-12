package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.member.entity.MemberUser;

public interface PostQueryService {
    PostResponse.PostPreviewListDTO getPostList(Integer page);

    PostResponse.PostDetailResDTO getPostDetail(Long postId, MemberUser memberUser);

    PostResponse.PostPreviewListDTO getPopularPosts(MemberUser memberUser);
}