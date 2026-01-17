package com.finders.api.infra.payment;

import com.finders.api.domain.payment.enums.PaymentMethod;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.enums.PgProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortOnePaymentInfo {

    private final String paymentId;
    private final String transactionId;
    private final PaymentStatus status;
    private final Integer amount;
    private final PaymentMethod method;
    private final PgProvider pgProvider;
    private final String pgTxId;
    private final String receiptUrl;

    // 카드 정보
    private final String cardCompany;
    private final String cardNumber;
    private final String approveNo;
    private final Integer installmentMonths;

    // 실패 정보
    private final String failCode;
    private final String failMessage;
}
