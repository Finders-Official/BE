package com.finders.api.domain.store.dto.response;

public record PhotoLabRegionCountResponse(
        Long regionId,
        String regionName,
        Long photoLabCount
) {
}
