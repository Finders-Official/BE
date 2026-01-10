package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.member.entity.MemberUser;

public interface PostCommandService {

    Long createPost(PostRequest.CreatePostDTO request, MemberUser memberUser);

    void deletePost(Long postId, MemberUser memberUser);
}