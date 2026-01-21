package com.finders.api.domain.store.entity;

import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "photo_lab",
        indexes = {
                @Index(name = "idx_lab_status", columnList = "status"),
                @Index(name = "idx_lab_location", columnList = "latitude, longitude")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoLab extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK → member(id)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private MemberOwner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

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

    @Column(name = "work_count", nullable = false)
    private Integer workCount = 0;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Column(name = "reservation_count", nullable = false)
    private Integer reservationCount = 0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "avg_work_time")
    private Integer avgWorkTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PhotoLabStatus status = PhotoLabStatus.PENDING;

    @Column(name = "is_delivery_available", nullable = false)
    private boolean isDeliveryAvailable = false;

    @Column(name = "max_reservations_per_hour", nullable = false)
    private Integer maxReservationsPerHour = 3;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    public void incrementReviewCount() {
        this.reviewCount++;
    }

    @Builder
    private PhotoLab(
            MemberOwner owner,
            Region region,
            String name,
            String description,
            String phone,
            String zipcode,
            String address,
            String addressDetail,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer workCount,
            Integer postCount,
            Integer reservationCount,
            Integer avgWorkTime,
            PhotoLabStatus status,
            Boolean isDeliveryAvailable,
            Integer maxReservationsPerHour,
            String qrCodeUrl
    ) {
        this.owner = owner;
        this.region = region;
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.workCount = workCount != null ? workCount : 0;
        this.postCount = postCount != null ? postCount : 0;
        this.reservationCount = reservationCount != null ? reservationCount : 0;
        this.avgWorkTime = avgWorkTime;
        this.status = status != null ? status : PhotoLabStatus.PENDING;
        this.isDeliveryAvailable = isDeliveryAvailable != null && isDeliveryAvailable;
        this.maxReservationsPerHour = maxReservationsPerHour != null ? maxReservationsPerHour : 3;
        this.qrCodeUrl = qrCodeUrl;
    }
}
