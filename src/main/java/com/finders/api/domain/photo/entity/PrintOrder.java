package com.finders.api.domain.photo.entity;

import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.enums.PrintOrderStatus;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "print_order",
        indexes = {
//                @Index(name = "idx_print_order_member", columnList = "member_id, status"),
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

    @Column(name = "order_code", length = 20, nullable = false)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PrintOrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_method", length = 20, nullable = false)
    private ReceiptMethod receiptMethod;

    @Column(name = "estimated_at")
    private LocalDateTime estimatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}
