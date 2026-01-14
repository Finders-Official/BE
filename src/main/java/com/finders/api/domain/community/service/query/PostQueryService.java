package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import org.springframework.data.domain.Pageable;

public interface PostQueryService {
    PostResponse.PostPreviewListDTO getPostList(Integer page, Long memberId);

    PostResponse.PostDetailResDTO getPostDetail(Long postId, Long memberId);

    PostResponse.PostPreviewListDTO getPopularPosts(Long memberId);

    PostResponse.PostPreviewListDTO searchPosts(String keyword, Long memberId, Pageable pageable);

    // 현상소 검색
    PostResponse.PhotoLabSearchListDTO searchPhotoLabs(String keyword, Double latitude, Double longitude, Pageable pageable, Long memberId);
}