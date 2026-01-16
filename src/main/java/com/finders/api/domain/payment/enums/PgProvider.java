package com.finders.api.domain.payment.enums;

/**
 * PG사/간편결제 제공자
 */
public enum PgProvider {
    // 메인 PG사
    KCP,

    // 간편결제
    KAKAOPAY,
    NAVERPAY,
    TOSSPAY,

    // PG사
    TOSSPAYMENTS
}
