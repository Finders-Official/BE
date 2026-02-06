package com.finders.api.domain.photo.entity;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "development_order",
        indexes = {
                @Index(name = "idx_dev_order_member", columnList = "member_id, status"),
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser user;

    @Column(name = "order_code", length = 20, nullable = false)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private DevelopmentOrderStatus status = DevelopmentOrderStatus.RECEIVED;

    @Column(name = "is_develop", nullable = false)
    private boolean isDevelop;

    @Column(name = "is_scan", nullable = false)
    private boolean isScan;

    @Column(name = "is_print", nullable = false)
    private boolean isPrint;

    @Column(name = "roll_count", nullable = false)
    private int rollCount;

    @Column(name = "total_photos", nullable = false)
    private int totalPhotos = 0;

    @Column(name = "total_price", nullable = false)
    private int totalPrice = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private DevelopmentOrder(
            Reservation reservation,
            PhotoLab photoLab,
            MemberUser user,
            String orderCode,
            DevelopmentOrderStatus status,
            boolean isDevelop,
            boolean isScan,
            boolean isPrint,
            int rollCount,
            int totalPhotos,
            int totalPrice,
            LocalDateTime completedAt
    ) {
        this.reservation = reservation;
        this.photoLab = photoLab;
        this.user = user;
        this.orderCode = orderCode;
        this.status = (status == null) ? DevelopmentOrderStatus.RECEIVED : status;
        this.isDevelop = isDevelop;
        this.isScan = isScan;
        this.isPrint = isPrint;
        this.rollCount = rollCount;
        this.totalPhotos = totalPhotos;
        this.totalPrice = totalPrice;
        this.completedAt = completedAt;
    }

    public static DevelopmentOrder create(
            PhotoLab photoLab,
            MemberUser memberUser,
            Reservation reservation,
            String orderCode,
            int totalPhotos,
            int totalPrice,
            List<String> taskTypes,
            int rollCount
    ) {
        // 예약 기반이면 reservation 값 우선 (정책)
        boolean develop, scan, print;
        int finalRollCount;

        if (reservation != null) {
            develop = reservation.isDevelop();
            scan = reservation.isScan();
            print = reservation.isPrint();
            finalRollCount = reservation.getRollCount();
        } else {
            // 비예약이면 입력값으로 세팅 + 검증
            if (taskTypes == null || taskTypes.isEmpty()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "예약이 없는 주문은 taskTypes가 필수입니다.");
            }
            if (rollCount < 1) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "예약이 없는 주문은 rollCount(1 이상)가 필수입니다.");
            }

            List<String> types = taskTypes.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toUpperCase())
                    .toList();

            develop = types.contains("DEVELOP");
            scan = types.contains("SCAN");
            print = types.contains("PRINT");
            finalRollCount = rollCount;
        }

        return DevelopmentOrder.builder()
                .photoLab(photoLab)
                .user(memberUser)
                .reservation(reservation)
                .orderCode(orderCode)
                .totalPhotos(totalPhotos)
                .totalPrice(totalPrice)
                .isDevelop(develop)
                .isScan(scan)
                .isPrint(print)
                .rollCount(finalRollCount)
                .status(DevelopmentOrderStatus.RECEIVED)
                .build();
    }


    public void updateStatus(DevelopmentOrderStatus status) {
        if(status == DevelopmentOrderStatus.COMPLETED){
            completedAt = LocalDateTime.now();
        }
        this.status = status;
    }

    public boolean hasPrintTask() {return this.isPrint;}
}
