package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.Region;
import com.finders.api.domain.store.dto.response.PhotoLabRegionItemResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegionRepository extends JpaRepository<Region, Long> {
    @Query("select new com.finders.api.domain.store.dto.response.PhotoLabRegionItemResponse(" +
            "r.id, r.regionName, r.parentRegion.id) " +
            "from Region r " +
            "where r.parentRegion is not null")
    List<PhotoLabRegionItemResponse> findAllRegionItems();
}
