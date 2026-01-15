package com.finders.api.domain.store.dto.response;

import lombok.Builder;
import java.util.List;

public class PhotoLabListResponse {

    @Builder
    public record Card(
            Long photoLabId,
            String name,
            List<String> imageUrls,
            List<String> tags,
            String address,
            Double distanceKm,
            boolean isFavorite,
            Integer workCount,
            Integer avgWorkTime
    ) {
        public static Card from(
                com.finders.api.domain.store.entity.PhotoLab photoLab,
                List<String> imageUrls,
                List<String> tags,
                Double distanceKm,
                boolean isFavorite
        ) {
            return Card.builder()
                    .photoLabId(photoLab.getId())
                    .name(photoLab.getName())
                    .imageUrls(imageUrls)
                    .tags(tags)
                    .address(photoLab.getAddress())
                    .distanceKm(distanceKm)
                    .isFavorite(isFavorite)
                    .workCount(photoLab.getWorkCount())
                    .avgWorkTime(photoLab.getAvgWorkTime())
                    .build();
        }
    }
}
