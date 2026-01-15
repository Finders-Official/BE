package com.finders.api.domain.store.service.command;

import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;

public interface PhotoLabFavoriteCommandService {

    PhotoLabFavoriteResponse.Status addFavorite(Long photoLabId, Long memberId);

    PhotoLabFavoriteResponse.Status removeFavorite(Long photoLabId, Long memberId);
}
