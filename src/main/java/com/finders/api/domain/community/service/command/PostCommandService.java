package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.dto.response.PostResponse;

public interface PostCommandService {

    PostResponse.PostDetailResDTO createPost(PostRequest.CreatePostDTO request, Long memberId);

    void deletePost(Long postId, Long memberId);
}