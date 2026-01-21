package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.enums.PostSearchFilter;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostQueryService {
    PostResponse.PostPreviewListDTO getPostList(Integer page, Integer size, Long memberId);

    PostResponse.PostDetailResDTO getPostDetail(Long postId, Long memberId);

    PostResponse.PostPreviewListDTO getPopularPosts(Long memberId);

    PostResponse.PostPreviewListDTO searchPosts(String keyword, PostSearchFilter filter, Long memberId, Pageable pageable);

    List<String> getAutocompleteSuggestions(String keyword);
}