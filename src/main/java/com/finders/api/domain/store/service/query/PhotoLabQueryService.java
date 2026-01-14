package com.finders.api.domain.store.service.query;

import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.global.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PhotoLabQueryService {

    PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(
            Long memberId,
            String query,
            List<Long> keywordIds,
            Long regionId,
            LocalDate date,
            Integer page,
            Integer size,
            Double lat,
            Double lng
    );

    // 커뮤니티 현상소 검색
    PhotoLabResponse.PhotoLabSearchListDTO searchPhotoLabs(String keyword, Double latitude, Double longitude, Pageable pageable, Long memberId);
}
