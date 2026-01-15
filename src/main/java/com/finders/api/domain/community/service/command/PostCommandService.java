package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;

public interface PostCommandService {

    Long createPost(PostRequest.CreatePostDTO request, Long memberId);

    void deletePost(Long postId, Long memberId);
}