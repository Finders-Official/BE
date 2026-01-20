package com.finders.api.domain.community.dto.response;

import com.finders.api.domain.community.entity.SearchHistory;
import lombok.Builder;

public class SearchHistoryResponse {

    @Builder
    public record SearchHistoryResDTO(
            Long id,
            String keyword,
            String imageUrl,
            Integer width,
            Integer height
    ) {
        public static SearchHistoryResDTO from(SearchHistory history, String imageUrl) {
            return SearchHistoryResDTO.builder()
                    .id(history.getId())
                    .keyword(history.getKeyword())
                    .imageUrl(imageUrl)
                    .width(history.getWidth())
                    .height(history.getHeight())
                    .build();
        }
    }
}
