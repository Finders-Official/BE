package com.finders.api.domain.store.dto.response;

import lombok.Builder;
import java.util.List;

public class PhotoLabPopularResponse {

    @Builder
    public record Card(
            Long photoLabId,
            String name,
            String mainImageObjectPath,
            List<String> tags,
            Integer workCount
    ) {
        public static Card from(
                com.finders.api.domain.store.entity.PhotoLab photoLab,
                String mainImageObjectPath,
                List<String> tags
        ) {
            return Card.builder()
                    .photoLabId(photoLab.getId())
                    .name(photoLab.getName())
                    .mainImageObjectPath(mainImageObjectPath)
                    .tags(tags)
                    .workCount(photoLab.getWorkCount())
                    .build();
        }
    }
}
