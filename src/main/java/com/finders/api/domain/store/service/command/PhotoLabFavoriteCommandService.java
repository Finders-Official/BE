package com.finders.api.domain.store.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;

public interface PhotoLabFavoriteCommandService {

    PhotoLabFavoriteResponse.Status addFavorite(Long photoLabId, MemberUser memberUser);

    PhotoLabFavoriteResponse.Status removeFavorite(Long photoLabId, MemberUser memberUser);
}
