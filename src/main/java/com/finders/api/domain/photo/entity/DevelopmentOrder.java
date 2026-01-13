package com.finders.api.domain.photo.entity;

import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.store.entity.PhotoLab;
//import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "development_order",
        indexes = {
//                @Index(name = "idx_dev_order_member", columnList = "member_id, status"),
                @Index(name = "idx_dev_order_lab", columnList = "photo_lab_id, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dev_order_code", columnNames = "order_code")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DevelopmentOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true)   // 1:1 보장
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

//유저에 대해 연관관계 매핑 해야함

    @Column(name = "order_code", length = 20, nullable = false)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private DevelopmentOrderStatus status = DevelopmentOrderStatus.RECEIVED;

    @Column(name = "total_photos", nullable = false)
    private int totalPhotos = 0;

    @Column(name = "total_price", nullable = false)
    private int totalPrice = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}
