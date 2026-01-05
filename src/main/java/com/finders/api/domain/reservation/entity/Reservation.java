package com.finders.api.domain.reservation.entity;

import com.finders.api.domain.reservation.enums.ReservationStatus;
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
}
