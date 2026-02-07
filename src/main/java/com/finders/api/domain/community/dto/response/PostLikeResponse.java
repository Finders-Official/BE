package com.finders.api.domain.community.dto.response;

import lombok.Builder;

public class PostLikeResponse {

    @Builder
    public record PostLikeResDTO(
            Long postId,
            Integer likeCount,
            boolean isLiked
    ) {}
}