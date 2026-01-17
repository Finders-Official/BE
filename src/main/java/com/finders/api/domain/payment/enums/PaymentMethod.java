package com.finders.api.domain.payment.enums;

/**
 * 포트원 V2 결제 수단
 */
public enum PaymentMethod {
    CARD,               // 카드 결제
    TRANSFER,           // 계좌이체
    VIRTUAL_ACCOUNT,    // 가상계좌
    EASY_PAY            // 간편결제 (카카오페이, 네이버페이, 토스페이 등)
}
