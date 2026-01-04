package com.finders.api.domain.reservation.entity;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
}
