package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.Delivery;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByPrintOrderId(Long printOrderId);
}
