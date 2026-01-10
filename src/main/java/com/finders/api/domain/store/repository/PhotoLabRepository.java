package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLab;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoLabRepository extends JpaRepository<PhotoLab, Long> {
}
