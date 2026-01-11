package com.finders.api.domain.reservation.repository;

import com.finders.api.domain.reservation.entity.Reservation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndPhotoLabIdAndUserId(Long reservationId, Long photoLabId, Long memberId);

    @Query("""
    select r from Reservation r
    join fetch r.photoLab pl
    join fetch r.slot s
    where r.id = :reservationId
      and pl.id = :photoLabId
      and r.user.id = :memberId
""")
    Optional<Reservation> findDetailByIdAndPhotoLabIdAndMemberId(
            @Param("reservationId") Long reservationId,
            @Param("photoLabId") Long photoLabId,
            @Param("memberId") Long memberId
    );
}
