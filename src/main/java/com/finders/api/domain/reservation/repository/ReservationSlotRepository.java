package com.finders.api.domain.reservation.repository;

import com.finders.api.domain.reservation.entity.ReservationSlot;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    List<ReservationSlot> findByPhotoLabIdAndReservationDate(Long photoLabId, LocalDate date);

    /**
     * (photoLabId, date, time) 슬롯을 비관적 락으로 조회
     * - 예약 생성 시 reservedCount 증가를 원자적으로 만들기 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from ReservationSlot s
        where s.photoLab.id = :photoLabId
          and s.reservationDate = :date
          and s.reservationTime = :time
    """)
    Optional<ReservationSlot> findByPhotoLabIdAndReservationDateAndReservationTimeForUpdate(
            Long photoLabId,
            LocalDate date,
            LocalTime time
    );

    /**
     * 슬롯 단건 비관적 락 조회
     * - 예약 취소 시 reservedCount 감소를 원자적으로 처리하기 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from ReservationSlot s
        where s.id = :slotId
    """)
    Optional<ReservationSlot> findByIdForUpdate(Long slotId);
}
