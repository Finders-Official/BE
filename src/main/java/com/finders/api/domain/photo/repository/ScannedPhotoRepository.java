package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.ScannedPhoto;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScannedPhotoRepository extends JpaRepository<ScannedPhoto, Long> {
    @Query("""
        select sp
        from ScannedPhoto sp
        where sp.order.id in :orderIds
        order by sp.displayOrder asc
    """)
    List<ScannedPhoto> findByOrderIdInOrderByDisplayOrderAsc(List<Long> orderIds);

    Slice<ScannedPhoto> findByOrderIdOrderByDisplayOrderAsc(Long orderId, Pageable pageable);
}
