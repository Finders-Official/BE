package com.finders.api.domain.reservation.entity;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.reservation.dto.ReservationRequest;
import com.finders.api.domain.reservation.enums.ReservationStatus;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "idx_reservation_lab_status", columnList = "photo_lab_id, status")
        }
)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private ReservationSlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReservationStatus status;

    @Column(name = "is_develop", nullable = false)
    private boolean isDevelop;

    @Column(name = "is_scan", nullable = false)
    private boolean isScan;

    @Column(name = "is_print", nullable = false)
    private boolean isPrint;

    @Column(name = "roll_count", nullable = false)
    private int rollCount;

    @Column(name = "request_message", length = 500)
    private String requestMessage;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Reservation(
            MemberUser user,
            ReservationSlot slot,
            PhotoLab photoLab,
            ReservationStatus status,
            boolean isDevelop,
            boolean isScan,
            boolean isPrint,
            int rollCount,
            String requestMessage,
            LocalDateTime deletedAt
    ) {
        this.user = user;
        this.slot = slot;
        this.photoLab = photoLab;
        this.status = status;
        this.isDevelop = isDevelop;
        this.isScan = isScan;
        this.isPrint = isPrint;
        this.rollCount = rollCount;
        this.requestMessage = requestMessage;
        this.deletedAt = deletedAt;
    }

    public static Reservation reserve(
            MemberUser user,
            ReservationSlot slot,
            PhotoLab photoLab,
            ReservationRequest.Create req
    ) {
        List<String> types = req.taskTypes();

        boolean develop = types != null && types.contains("DEVELOP");
        boolean scan = types != null && types.contains("SCAN");
        boolean print = types != null && types.contains("PRINT");

        return Reservation.builder()
                .user(user)
                .slot(slot)
                .photoLab(photoLab)
                .status(ReservationStatus.RESERVED)
                .isDevelop(develop)
                .isScan(scan)
                .isPrint(print)
                .rollCount(req.filmCount())
                .requestMessage(req.memo())
                .deletedAt(null)
                .build();
    }


    /**
     * 예약 취소
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.deletedAt = LocalDateTime.now();
    }
}

