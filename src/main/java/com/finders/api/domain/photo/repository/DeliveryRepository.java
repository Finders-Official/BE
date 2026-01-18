package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.Delivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByPrintOrderId(Long printOrderId);

    @Query("select d from Delivery d join fetch d.printOrder po where po.id in :printOrderIds")
    List<Delivery> findByPrintOrderIdIn(@Param("printOrderIds") List<Long> printOrderIds);
}
