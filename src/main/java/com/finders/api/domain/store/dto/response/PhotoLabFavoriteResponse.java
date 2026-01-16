package com.finders.api.domain.store.dto.response;

public class PhotoLabFavoriteResponse {

    public record Status(
            Long photoLabId,
            boolean isFavorite
    ) {
    }
}
