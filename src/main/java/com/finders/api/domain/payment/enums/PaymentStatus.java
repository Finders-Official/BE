package com.finders.api.domain.payment.enums;

/**
 * 포트원 V2 결제 상태
 */
public enum PaymentStatus {
    READY,                  // 결제 대기
    PENDING,                // 결제 진행 중
    VIRTUAL_ACCOUNT_ISSUED, // 가상계좌 발급됨
    PAID,                   // 결제 완료
    FAILED,                 // 결제 실패
    PARTIAL_CANCELLED,      // 부분 취소
    CANCELLED               // 전액 취소
}
