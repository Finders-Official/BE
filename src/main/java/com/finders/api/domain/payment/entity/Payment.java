package com.finders.api.domain.payment.entity;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.payment.enums.OrderType;
import com.finders.api.domain.payment.enums.PaymentMethod;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.enums.PgProvider;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_member", columnList = "member_id, status"),
        @Index(name = "idx_payment_order_type", columnList = "order_type, related_order_id"),
        @Index(name = "idx_payment_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 주문 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(name = "related_order_id")
    private Long relatedOrderId;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    @Column(name = "order_name", nullable = false, length = 100)
    private String orderName;

    // 금액 정보
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "token_amount")
    private Integer tokenAmount;

    // 포트원 V2 정보 (승인 후 저장)
    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "pg_tx_id", length = 100)
    private String pgTxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", length = 30)
    private PgProvider pgProvider;

    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    // 카드 정보
    @Column(name = "card_company", length = 20)
    private String cardCompany;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "approve_no", length = 20)
    private String approveNo;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    // 시간 정보
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // 실패/취소 정보
    @Column(name = "fail_code", length = 50)
    private String failCode;

    @Column(name = "fail_message", length = 200)
    private String failMessage;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "cancel_amount")
    private Integer cancelAmount;

    @Builder
    private Payment(Member member, OrderType orderType, Long relatedOrderId,
                    String paymentId, String orderName, Integer amount, Integer tokenAmount) {
        this.member = member;
        this.orderType = orderType;
        this.relatedOrderId = relatedOrderId;
        this.paymentId = paymentId;
        this.orderName = orderName;
        this.amount = amount;
        this.tokenAmount = tokenAmount;
        this.status = PaymentStatus.READY;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * 결제 완료 처리 (포트원 조회 결과 반영)
     */
    public void complete(String transactionId, String pgTxId, PgProvider pgProvider,
                         PaymentMethod method, String cardCompany, String cardNumber,
                         String approveNo, Integer installmentMonths, String receiptUrl) {
        this.transactionId = transactionId;
        this.pgTxId = pgTxId;
        this.pgProvider = pgProvider;
        this.method = method;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.approveNo = approveNo;
        this.installmentMonths = installmentMonths;
        this.receiptUrl = receiptUrl;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리
     */
    public void fail(String failCode, String failMessage) {
        this.failCode = failCode;
        this.failMessage = failMessage;
        this.status = PaymentStatus.FAILED;
    }

    /**
     * 결제 취소 처리
     */
    public void cancel(String cancelReason, Integer cancelAmount) {
        this.cancelReason = cancelReason;
        this.cancelAmount = cancelAmount;
        this.cancelledAt = LocalDateTime.now();
        this.status = (cancelAmount != null && cancelAmount < this.amount)
                ? PaymentStatus.PARTIAL_CANCELLED
                : PaymentStatus.CANCELLED;
    }

    /**
     * 가상계좌 발급 상태로 변경
     */
    public void issueVirtualAccount(String transactionId, String pgTxId, PgProvider pgProvider) {
        this.transactionId = transactionId;
        this.pgTxId = pgTxId;
        this.pgProvider = pgProvider;
        this.method = PaymentMethod.VIRTUAL_ACCOUNT;
        this.status = PaymentStatus.VIRTUAL_ACCOUNT_ISSUED;
    }

    /**
     * 상태 업데이트 (웹훅 등에서 사용)
     */
    public void updateStatus(PaymentStatus status) {
        this.status = status;
        if (status == PaymentStatus.PAID) {
            this.paidAt = LocalDateTime.now();
        }
    }
}
