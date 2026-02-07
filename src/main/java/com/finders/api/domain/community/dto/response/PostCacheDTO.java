package com.finders.api.domain.community.dto.response;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PostCacheDTO(
        Long postId,
        String title,
        Integer likeCount,
        Integer commentCount,
        String objectPath
) implements Serializable {
}