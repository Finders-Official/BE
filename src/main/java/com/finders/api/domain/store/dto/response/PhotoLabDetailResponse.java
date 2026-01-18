package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.enums.NoticeType;
import java.util.List;
import lombok.Builder;

public class PhotoLabDetailResponse {

    @Builder
    public record Detail(
            Long photoLabId,
            String name,
            List<String> imageObjectPaths,
            List<String> tags,
            String address,
            String addressDetail,
            Double distanceKm,
            boolean isFavorite,
            Integer workCount,
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
