package com.finders.api.domain.store.dto.response;

import java.util.List;

public class PhotoLabPopularResponse {

    public record Card(
            Long photoLabId,
            String name,
            String mainImageUrl,
            List<String> keywords,
            Integer workCount
    ) {
    }
}
