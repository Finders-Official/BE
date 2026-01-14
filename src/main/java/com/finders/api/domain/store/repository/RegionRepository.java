package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
