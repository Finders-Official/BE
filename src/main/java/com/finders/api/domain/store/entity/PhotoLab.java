package com.finders.api.domain.store.entity;

import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.finders.api.global.entity.BaseEntity;
//import com.finders.api.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "photo_lab",
        indexes = {
                @Index(name = "idx_lab_status", columnList = "status"),
                @Index(name = "idx_lab_rating", columnList = "rating"),
                @Index(name = "idx_lab_location", columnList = "latitude, longitude")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhotoLab extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK → member(id)
     * TODO: Member 엔티티 실제 패키지 경로에 맞게 import/타입 조정
     * 상위 import문에 주석 제거로 처리
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String zipcode;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(name = "address_detail", length = 100)
    private String addressDetail;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Builder.Default
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating = new BigDecimal("0.0");

    @Builder.Default
    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Builder.Default
    @Column(name = "reservation_count", nullable = false)
    private Integer reservationCount = 0;

    @Column(name = "avg_work_time")
    private Integer avgWorkTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PhotoLabStatus status = PhotoLabStatus.PENDING;

    @Builder.Default
    @Column(name = "is_delivery_available", nullable = false)
    private boolean isDeliveryAvailable = false;

    @Builder.Default
    @Column(name = "max_reservations_per_hour", nullable = false)
    private Integer maxReservationsPerHour = 3;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;
}
