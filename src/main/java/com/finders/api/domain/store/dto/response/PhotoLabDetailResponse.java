package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.enums.NoticeType;
import java.util.List;
import lombok.Builder;

public class PhotoLabDetailResponse {

    @Builder
    public record Detail(
            Long photoLabId,
            String name,
            List<String> imageUrls,
            List<String> tags,
            String address,
            String addressDetail,
            Double latitude,
            Double longitude,
            Double distanceKm,
            boolean isFavorite,
            Integer workCount,
            Integer reviewCount,
            Integer avgWorkTime,
            Notice mainNotice,
            List<String> postImageUrls
    ) {
    }

    public record Notice(
            NoticeType noticeType,
            String title
    ) {
    }
}
