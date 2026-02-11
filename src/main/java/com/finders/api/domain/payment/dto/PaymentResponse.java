package com.finders.api.domain.payment.dto;

import com.finders.api.domain.payment.entity.Payment;
import com.finders.api.domain.payment.enums.OrderType;
import com.finders.api.domain.payment.enums.PaymentMethod;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.enums.PgProvider;
import lombok.Builder;

import java.time.LocalDateTime;

public class PaymentResponse {

    @Builder
    public record PreRegistered(
            Long id,
            String paymentId,
            String orderName,
            Integer amount,
            PaymentStatus status
    ) {}

    @Builder
    public record Detail(
            Long id,
            String paymentId,
            String transactionId,
            OrderType orderType,
            Long relatedOrderId,
            String orderName,
            Integer amount,
            Integer creditAmount,
            PaymentStatus status,
            PaymentMethod method,
            PgProvider pgProvider,
            String cardCompany,
            String cardNumber,
            String approveNo,
            Integer installmentMonths,
            String receiptUrl,
            LocalDateTime requestedAt,
            LocalDateTime paidAt,
            String failCode,
            String failMessage,
            LocalDateTime cancelledAt,
            String cancelReason,
            Integer cancelAmount,
            LocalDateTime createdAt
    ) {
        public static Detail from(Payment payment) {
            return Detail.builder()
                    .id(payment.getId())
                    .paymentId(payment.getPaymentId())
                    .transactionId(payment.getTransactionId())
                    .orderType(payment.getOrderType())
                    .relatedOrderId(payment.getRelatedOrderId())
                    .orderName(payment.getOrderName())
                    .amount(payment.getAmount())
                    .creditAmount(payment.getCreditAmount())
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .pgProvider(payment.getPgProvider())
                    .cardCompany(payment.getCardCompany())
                    .cardNumber(payment.getCardNumber())
                    .approveNo(payment.getApproveNo())
                    .installmentMonths(payment.getInstallmentMonths())
                    .receiptUrl(payment.getReceiptUrl())
                    .requestedAt(payment.getRequestedAt())
                    .paidAt(payment.getPaidAt())
                    .failCode(payment.getFailCode())
                    .failMessage(payment.getFailMessage())
                    .cancelledAt(payment.getCancelledAt())
                    .cancelReason(payment.getCancelReason())
                    .cancelAmount(payment.getCancelAmount())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Cancelled(
            Long id,
            String paymentId,
            PaymentStatus status,
            Integer cancelAmount,
            String cancelReason,
            LocalDateTime cancelledAt
    ) {}

    @Builder
    public record Summary(
            Long id,
            String paymentId,
            String orderName,
            Integer amount,
            PaymentStatus status,
            PaymentMethod method,
            LocalDateTime paidAt
    ) {
        public static Summary from(Payment payment) {
            return Summary.builder()
                    .id(payment.getId())
                    .paymentId(payment.getPaymentId())
                    .orderName(payment.getOrderName())
                    .amount(payment.getAmount())
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .paidAt(payment.getPaidAt())
                    .build();
        }
    }
}
