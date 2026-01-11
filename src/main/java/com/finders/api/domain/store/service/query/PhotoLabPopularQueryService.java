package com.finders.api.domain.store.service.query;

import com.finders.api.domain.store.dto.response.PhotoLabPopularResponse;

import java.util.List;

public interface PhotoLabPopularQueryService {
    List<PhotoLabPopularResponse.Card> getPopularPhotoLabs();
}
