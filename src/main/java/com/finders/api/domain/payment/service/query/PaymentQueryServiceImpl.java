package com.finders.api.domain.payment.service.query;

import com.finders.api.domain.payment.dto.PaymentResponse;
import com.finders.api.domain.payment.entity.Payment;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.repository.PaymentRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse.Detail getPayment(Long memberId, String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제인지 확인
        if (!payment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        return PaymentResponse.Detail.from(payment);
    }

    @Override
    public List<PaymentResponse.Summary> getMyPayments(Long memberId) {
        return paymentRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(PaymentResponse.Summary::from)
                .toList();
    }

    @Override
    public List<PaymentResponse.Summary> getMyPaymentsByStatus(Long memberId, PaymentStatus status) {
        return paymentRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, status)
                .stream()
                .map(PaymentResponse.Summary::from)
                .toList();
    }
}
