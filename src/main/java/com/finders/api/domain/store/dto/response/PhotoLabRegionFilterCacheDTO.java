package com.finders.api.domain.store.dto.response;

import java.util.List;

public record PhotoLabRegionFilterCacheDTO(
        List<Parent> parents,
        List<Region> regions
) {
    public static PhotoLabRegionFilterCacheDTO from(PhotoLabRegionFilterResponse response) {
        List<Parent> parentItems = response.parents().stream()
                .map(item -> new Parent(item.parentId(), item.parentName(), item.photoLabCount()))
                .toList();
        List<Region> regionItems = response.regions().stream()
                .map(item -> new Region(item.regionId(), item.regionName(), item.parentId()))
                .toList();
        return new PhotoLabRegionFilterCacheDTO(parentItems, regionItems);
    }

    public PhotoLabRegionFilterResponse toResponse() {
        List<PhotoLabParentRegionCountResponse> parentItems = parents.stream()
                .map(item -> new PhotoLabParentRegionCountResponse(item.parentId(), item.parentName(), item.photoLabCount()))
                .toList();
        List<PhotoLabRegionItemResponse> regionItems = regions.stream()
                .map(item -> new PhotoLabRegionItemResponse(item.regionId(), item.regionName(), item.parentId()))
                .toList();
        return new PhotoLabRegionFilterResponse(parentItems, regionItems);
    }

    public record Parent(
            Long parentId,
            String parentName,
            Long photoLabCount
    ) {
    }

    public record Region(
            Long regionId,
            String regionName,
            Long parentId
    ) {
    }
}
