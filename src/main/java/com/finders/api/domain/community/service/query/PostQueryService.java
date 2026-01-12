package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.domain.Pageable;

public interface PostQueryService {
    PostResponse.PostPreviewListDTO getPostList(Integer page);

    PostResponse.PostDetailResDTO getPostDetail(Long postId, MemberUser memberUser);

    PostResponse.PostPreviewListDTO getPopularPosts(MemberUser memberUser);

    PostResponse.PostPreviewListDTO searchPosts(String keyword, MemberUser memberUser, Pageable pageable);
}