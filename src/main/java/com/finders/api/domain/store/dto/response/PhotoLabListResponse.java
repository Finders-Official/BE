package com.finders.api.domain.store.dto.response;

import java.util.List;

public class PhotoLabListResponse {

    public record Card(
            Long photoLabId,
            String name,
            List<String> imageUrls,
            List<String> keywords,
            String address,
            Double distanceKm,
            boolean isFavorite,
            Integer workCount,
            Integer avgWorkTime
    ) {
    }
}
