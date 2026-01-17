package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoLabRepository extends JpaRepository<PhotoLab, Long> {
    List<PhotoLab> findTop8ByOrderByReservationCountDescIdAsc();

    Optional<PhotoLab> findByIdAndStatus(Long id, PhotoLabStatus status);

    // 커뮤니티 현상소 검색
    @Query("SELECT pl FROM PhotoLab pl " +
            "WHERE pl.status = 'ACTIVE' " +
            "AND pl.name LIKE %:keyword%")
    Page<PhotoLab> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
