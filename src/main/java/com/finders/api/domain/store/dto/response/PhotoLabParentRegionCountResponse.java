package com.finders.api.domain.store.dto.response;

public record PhotoLabParentRegionCountResponse(
        Long parentId,
        String parentName,
        Long photoLabCount
) {
}
