package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.PrintOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long> {
    Optional<PrintOrder> findByIdAndUserId(Long id, Long memberId);
}
