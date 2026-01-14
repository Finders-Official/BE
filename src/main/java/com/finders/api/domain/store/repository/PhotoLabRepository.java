package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoLabRepository extends JpaRepository<PhotoLab, Long> {
    List<PhotoLab> findTop8ByOrderByReservationCountDescIdAsc();
}
