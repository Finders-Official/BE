package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.repository.projection.ScanPreviewProjection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScannedPhotoRepository extends JpaRepository<ScannedPhoto, Long> {
    @Query(value = """
        SELECT t.order_id AS orderId, t.image_key AS imageKey, t.display_order AS displayOrder
        FROM (
            SELECT sp.order_id,
                   sp.image_key,
                   sp.display_order,
                   ROW_NUMBER() OVER (PARTITION BY sp.order_id ORDER BY sp.display_order ASC) AS rn
            FROM scanned_photo sp
            WHERE sp.order_id IN (:orderIds)
        ) t
        WHERE t.rn <= :limit
        """, nativeQuery = true)
    List<ScanPreviewProjection> findPreviewByOrderIds(
            @Param("orderIds") List<Long> orderIds,
            @Param("limit") int limit
    );

    Slice<ScannedPhoto> findByOrderIdOrderByDisplayOrderAsc(Long orderId, Pageable pageable);

}
