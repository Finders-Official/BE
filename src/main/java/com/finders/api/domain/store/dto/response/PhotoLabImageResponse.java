package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.entity.PhotoLabImage;
import lombok.Builder;

public class PhotoLabImageResponse {

    @Builder
    public record Create(
            Long id,
            Long photoLabId,
            String imageUrl,
            String objectPath,
            Integer displayOrder,
            boolean isMain
    ) {
        public static Create from(PhotoLabImage image) {
            return Create.builder()
                    .id(image.getId())
                    .photoLabId(image.getPhotoLab().getId())
                    .objectPath(image.getObjectPath())
                    .displayOrder(image.getDisplayOrder())
                    .isMain(image.isMain())
                    .build();
        }
    }
}
