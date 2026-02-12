package com.finders.api.domain.reservation.repository;

import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.enums.ReservationStatus;
import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    /**
     * 예약 취소 시 중복 요청(동시 요청)으로 인한 reservedCount 과감소를 방지하기 위해
     * Reservation row 자체를 비관적 락으로 조회합니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r from Reservation r
        where r.id = :reservationId
          and r.photoLab.id = :photoLabId
          and r.user.id = :memberId
    """)
    Optional<Reservation> findByIdAndPhotoLabIdAndUserIdForUpdate(
            Long reservationId,
            Long photoLabId,
            Long memberId
    );

    @Query("""
    select r from Reservation r
    join fetch r.photoLab pl
    join fetch r.slot s
    join fetch r.user u
    where r.id = :reservationId
      and pl.id = :photoLabId
      and r.user.id = :memberId
""")
    Optional<Reservation> findDetailByIdAndPhotoLabIdAndMemberId(
            @Param("reservationId") Long reservationId,
            @Param("photoLabId") Long photoLabId,
            @Param("memberId") Long memberId
    );

    // 특정 회원의 예약 중 지정된 상태들에 해당하는 예약이 있는지 확인
    boolean existsByUserIdAndStatusIn(Long userId, Collection<ReservationStatus> statuses);
}
