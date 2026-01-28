package com.finders.api.domain.store.service.query;

import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.request.PhotoLabSearchCondition;
import com.finders.api.domain.store.dto.response.PhotoLabDetailResponse;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;
import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.global.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface PhotoLabQueryService {

    PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(
            PhotoLabSearchCondition condition
    );

    PhotoLabDetailResponse.Detail getPhotoLabDetail(Long photoLabId, Long memberId, Double lat, Double lng);

    // 커뮤니티 현상소 검색
    PhotoLabResponse.PhotoLabSearchListDTO searchCommunityPhotoLabs(
            PhotoLabRequest.PhotoLabCommunitySearchRequest request
    );

    // 관심 현상소 조회
    PhotoLabFavoriteResponse.SliceResponse getFavoritePhotoLabs(Long memberId, int page, int size, Double lat, Double lng);
}
