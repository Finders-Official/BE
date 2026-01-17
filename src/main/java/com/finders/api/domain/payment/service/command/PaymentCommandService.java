package com.finders.api.domain.payment.service.command;

import com.finders.api.domain.payment.dto.PaymentRequest;
import com.finders.api.domain.payment.dto.PaymentResponse;

public interface PaymentCommandService {

    /**
     * 결제 사전등록 (프론트에서 결제창 호출 전 호출)
     */
    PaymentResponse.PreRegistered preRegister(Long memberId, PaymentRequest.PreRegister request);

    /**
     * 결제 완료 처리 (프론트에서 결제 완료 후 호출)
     */
    PaymentResponse.Detail complete(Long memberId, PaymentRequest.Complete request);

    /**
     * 결제 취소
     */
    PaymentResponse.Cancelled cancel(Long memberId, String paymentId, PaymentRequest.Cancel request);

    /**
     * 웹훅 처리
     */
    void handleWebhook(String body, String webhookId, String webhookTimestamp, String webhookSignature);
}
