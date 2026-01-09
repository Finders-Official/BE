package com.finders.api.domain.reservation.service.command;

import com.finders.api.domain.reservation.dto.ReservationRequest;
import com.finders.api.domain.reservation.dto.ReservationResponse;

public interface ReservationCommandService {

    ReservationResponse.Created createReservation(Long photoLabId, Long memberId, ReservationRequest.Create request);

    ReservationResponse.Cancel cancelReservation(Long photoLabId, Long reservationId, Long memberId);
}
