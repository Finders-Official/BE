package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.DevelopmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevelopmentOrderRepository extends JpaRepository<DevelopmentOrder, Long> {
    /**
     * 주문 코드 중복 여부 확인
     */
    boolean existsByOrderCode(String orderCode);

    boolean existsByReservationId(Long reservationId);
}
