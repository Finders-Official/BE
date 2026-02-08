package com.finders.api.domain.photo.entity;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.photo.enums.PrintOrderStatus;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "print_order",
        indexes = {
                @Index(name = "idx_print_order_member", columnList = "member_id, status"),
                @Index(name = "idx_print_order_lab", columnList = "photo_lab_id, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_print_order_code", columnNames = "order_code")
        }
)
public class PrintOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dev_order_id")
    private DevelopmentOrder developmentOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser user;

    @Column(name = "order_code", length = 20, nullable = false)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PrintOrderStatus status;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_method", length = 20, nullable = false)
    private ReceiptMethod receiptMethod;

    // 입금 캡처 object path (private bucket)
    @Column(name = "deposit_receipt_object_path", length = 500)
    private String depositReceiptObjectPath;

    // 입금자명
    @Column(name = "depositor_name", length = 50)
    private String depositorName;

    // 입금 은행
    @Column(name = "deposit_bank_name", length = 50)
    private String depositBankName;

    // 입금 증빙 제출 시각
    @Column(name = "payment_submitted_at")
    private LocalDateTime paymentSubmittedAt;

    @Column(name = "estimated_at")
    private LocalDateTime estimatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private PrintOrder(
            DevelopmentOrder developmentOrder,
            PhotoLab photoLab,
            MemberUser user,
            String orderCode,
            PrintOrderStatus status,
            int totalPrice,
            ReceiptMethod receiptMethod,
            LocalDateTime estimatedAt,
            LocalDateTime completedAt
    ) {
        this.developmentOrder = developmentOrder;
        this.photoLab = photoLab;
        this.user = user;
        this.orderCode = orderCode;
        this.status = status;
        this.totalPrice = totalPrice;
        this.receiptMethod = receiptMethod;
        this.estimatedAt = estimatedAt;
        this.completedAt = completedAt;
    }

    public static PrintOrder create(
            DevelopmentOrder developmentOrder,
            PhotoLab photoLab,
            MemberUser user,
            ReceiptMethod receiptMethod,
            int totalPrice
    ) {
        PrintOrder order = PrintOrder.builder()
                .developmentOrder(developmentOrder)
                .photoLab(photoLab)
                .user(user)
                .receiptMethod(receiptMethod)
                .totalPrice(totalPrice)
                .status(PrintOrderStatus.PENDING)
                .build();

        // orderCode는 엔티티 내부 책임
        order.orderCode = order.generateOrderCode("PO");

        return order;
    }

    public void confirmDepositReceipt(
            String depositReceiptObjectPath,
            String depositorName,
            String depositBankName
    ) {
        // 이미 제출된 경우 방어
        if (this.status != PrintOrderStatus.PENDING) {
            throw new CustomException(ErrorCode.PHOTO_PRINT_ORDER_STATUS_INVALID);
        }

        this.depositReceiptObjectPath = depositReceiptObjectPath;
        this.depositorName = depositorName;
        this.depositBankName = depositBankName;
        this.paymentSubmittedAt = LocalDateTime.now();

        this.status = PrintOrderStatus.CONFIRMED;
    }

    public void startPrinting(LocalDateTime estimatedAt) {
        if (this.status != PrintOrderStatus.CONFIRMED) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "인화를 시작할 수 없는 상태입니다."
            );
        }

        this.estimatedAt = estimatedAt;
        this.status = PrintOrderStatus.PRINTING;
    }

    public void updateStatusByOwner(PrintOrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "status는 필수입니다."
            );
        }

        // 완료 찍으면 completedAt만 기록
        if (targetStatus == PrintOrderStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }

        this.status = targetStatus;
    }

    private String generateOrderCode(String prefix) {
        // PO-250116-8F3A9C
        String date = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));

        String random = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        return prefix + "-" + date + "-" + random;
    }
}
