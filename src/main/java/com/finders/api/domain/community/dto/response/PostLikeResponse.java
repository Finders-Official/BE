package com.finders.api.domain.community.dto.response;

import lombok.Builder;

public class PostLikeResponse {

    @Builder
    public record PostLikeResDTO(
            Integer likeCount,
            boolean isLiked
    ) {}
}