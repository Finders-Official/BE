package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.member.entity.Member;

public interface PostCommandService {

    Long createPost(PostRequest.CreatePostDTO request, Member member);

    void deletePost(Long postId, Member member);
}