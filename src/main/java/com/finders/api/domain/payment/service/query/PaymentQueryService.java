package com.finders.api.domain.payment.service.query;

import com.finders.api.domain.payment.dto.PaymentResponse;
import com.finders.api.domain.payment.enums.PaymentStatus;

import java.util.List;

public interface PaymentQueryService {

    /**
     * 결제 상세 조회
     */
    PaymentResponse.Detail getPayment(Long memberId, String paymentId);

    /**
     * 내 결제 목록 조회
     */
    List<PaymentResponse.Summary> getMyPayments(Long memberId);

    /**
     * 내 결제 목록 조회 (상태별)
     */
    List<PaymentResponse.Summary> getMyPaymentsByStatus(Long memberId, PaymentStatus status);
}
