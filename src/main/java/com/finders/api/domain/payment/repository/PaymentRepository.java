package com.finders.api.domain.payment.repository;

import com.finders.api.domain.payment.entity.Payment;
import com.finders.api.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"member"})
    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, PaymentStatus status);

    List<Payment> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByPaymentId(String paymentId);
}
