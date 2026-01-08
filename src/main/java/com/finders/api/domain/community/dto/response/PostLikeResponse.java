package com.finders.api.domain.community.dto.response;

import lombok.Builder;

public class PostLikeResponse {

    // 좋아요
    @Builder
    public record PostLikeDTO(
            Integer likeCount,
            boolean isLiked
    ) {}

    // 좋아요 취소
    @Builder
    public record PostUnlikeDTO(
            Integer likeCount,
            boolean isLiked
    ) {}
}