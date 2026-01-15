package com.finders.api.domain.store.dto.response;

import lombok.Builder;

public class PhotoLabFavoriteResponse {

    @Builder
    public record Status(
            Long photoLabId,
            boolean isFavorite
    ) {
    }
}
