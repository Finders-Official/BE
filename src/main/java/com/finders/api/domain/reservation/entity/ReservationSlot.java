package com.finders.api.domain.reservation.entity;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reservation_slot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_slot_lab_date_time",
                        columnNames = {
                                "photo_lab_id",
                                "reservation_date",
                                "reservation_time"
                        }
                )
        }
)
public class ReservationSlot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "reserved_count", nullable = false)
    private int reservedCount;

    @Builder
    private ReservationSlot(PhotoLab photoLab,
                            LocalDate reservationDate,
                            LocalTime reservationTime,
                            int maxCapacity,
                            int reservedCount) {
        this.photoLab = photoLab;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.maxCapacity = maxCapacity;
        this.reservedCount = reservedCount;
    }

    public static ReservationSlot create(PhotoLab photoLab, LocalDate date, LocalTime time, int maxCapacity) {
        return ReservationSlot.builder()
                .photoLab(photoLab)
                .reservationDate(date)
                .reservationTime(time)
                .maxCapacity(maxCapacity)
                .reservedCount(0)
                .build();
    }

    /**
     * 정원 초과 방지 + 증가
     */
    public void increaseReservedCountOrThrow() {
        if (this.reservedCount >= this.maxCapacity) {
            throw new CustomException(ErrorCode.RESERVATION_FULL);
        }
        this.reservedCount += 1;
    }

    /**
     * 안전 감소 (0 아래로 내려가지 않게)
     */
    public void decreaseReservedCountSafely() {
        if (this.reservedCount <= 0) return;
        this.reservedCount -= 1;
    }
}
