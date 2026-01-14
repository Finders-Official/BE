package com.finders.api.domain.store.dto.response;

import lombok.Builder;
import java.util.List;

public class PhotoLabPopularResponse {

    @Builder
    public record Card(
            Long photoLabId,
            String name,
            String mainImageUrl,
            List<String> keywords,
            Integer workCount
    ) {
    }
}
