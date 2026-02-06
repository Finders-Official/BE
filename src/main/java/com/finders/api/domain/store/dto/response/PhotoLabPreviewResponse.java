package com.finders.api.domain.store.dto.response;

import lombok.Builder;

public class PhotoLabPreviewResponse {

    @Builder
    public record Card(
            Long photoLabId,
            String name,
            String imageUrl,
            String address
    ) {
        public static Card from(
                com.finders.api.domain.store.entity.PhotoLab photoLab,
                String imageUrl
        ) {
            return Card.builder()
                    .photoLabId(photoLab.getId())
                    .name(photoLab.getName())
                    .imageUrl(imageUrl)
                    .address(photoLab.getAddress())
                    .build();
        }
    }
}
