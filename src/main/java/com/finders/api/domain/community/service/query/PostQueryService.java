package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.member.entity.Member;

public interface PostQueryService {
    PostResponse.PostPreViewListDTO getPostList(Integer page);

    PostResponse.PostDetailResDTO getPostDetail(Long postId, Member member);

    PostResponse.PostPreViewListDTO getPopularPosts();
}