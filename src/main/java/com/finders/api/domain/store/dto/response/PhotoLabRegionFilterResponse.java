package com.finders.api.domain.store.dto.response;

import java.util.List;

public record PhotoLabRegionFilterResponse(
        List<PhotoLabParentRegionCountResponse> parents,
        List<PhotoLabRegionItemResponse> regions
) {
}
