package com.finders.api.domain.store.service.query;

import com.finders.api.domain.store.dto.request.PhotoLabSearchCondition;
import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.global.response.PagedResponse;

public interface PhotoLabQueryService {

    PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(
            PhotoLabSearchCondition condition
    );
}
