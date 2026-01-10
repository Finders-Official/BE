package com.finders.api.domain.reservation.service.query;

import com.finders.api.domain.reservation.dto.ReservationResponse;

import java.time.LocalDate;

public interface ReservationQueryService {

    ReservationResponse.AvailableTimes getAvailableTimes(Long photoLabId, LocalDate date);

    ReservationResponse.Detail getReservation(Long photoLabId, Long reservationId, Long memberId);
}
