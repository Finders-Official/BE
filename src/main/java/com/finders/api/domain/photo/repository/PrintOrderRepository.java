package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.PrintOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long> {
    Optional<PrintOrder> findByIdAndUserId(Long id, Long memberId);

    @Query("select po from PrintOrder po join fetch po.developmentOrder d where d.id in :devOrderIds")
    List<PrintOrder> findByDevelopmentOrderIdIn(@Param("devOrderIds") List<Long> devOrderIds);

    Optional<PrintOrder> findByDevelopmentOrderId(Long developmentOrderId);
}
