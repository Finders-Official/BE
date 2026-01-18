package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.PrintOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintOrderItemRepository extends JpaRepository<PrintOrderItem, Long> {
}
