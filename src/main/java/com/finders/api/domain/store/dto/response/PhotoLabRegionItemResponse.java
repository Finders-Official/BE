package com.finders.api.domain.store.dto.response;

public record PhotoLabRegionItemResponse(
        Long regionId,
        String regionName,
        Long parentId
) {
}
