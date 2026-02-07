package com.finders.api.domain.reservation.controller;

import com.finders.api.domain.reservation.dto.ReservationRequest;
import com.finders.api.domain.reservation.dto.ReservationResponse;
import com.finders.api.domain.reservation.service.command.ReservationCommandService;
import com.finders.api.domain.reservation.service.query.ReservationQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * 현상소 예약 API 컨트롤러
 */
@Tag(name = "PhotoLab Reservation", description = "현상소 예약 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/photo-labs")
public class ReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @Operation(
            summary = "현상소 날짜별 예약 가능 시간대 조회",
            description = "특정 날짜에 예약 가능한 시간대 목록을 조회합니다."
    )
    @GetMapping("/{photoLabId}/reservations/available-times")
    public ApiResponse<ReservationResponse.AvailableTimes> getAvailableTimes(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long photoLabId,
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE) LocalDate date
    ) {
        ReservationResponse.AvailableTimes response = reservationQueryService.getAvailableTimes(photoLabId, date);
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @Operation(
            summary = "현상소 예약 등록",
            description = "선택한 시간대로 현상소 예약을 생성합니다."
    )
    @PostMapping("/{photoLabId}/reservations")
    public ApiResponse<ReservationResponse.Created> createReservation(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long photoLabId,
            @RequestBody @Valid ReservationRequest.Create request
    ) {
        ReservationResponse.Created response = reservationCommandService.createReservation(photoLabId, user.memberId(), request);
        return ApiResponse.success(SuccessCode.RESERVATION_CREATED, response);
    }

    @Operation(
            summary = "현상소 예약내역 완료 조회",
            description = "예약 완료 상세 정보를 조회합니다."
    )
    @GetMapping("/{photoLabId}/reservations/{reservationId}")
    public ApiResponse<ReservationResponse.Detail> getReservation(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long photoLabId,
            @PathVariable Long reservationId
    ) {
        ReservationResponse.Detail response = reservationQueryService.getReservation(photoLabId, reservationId,
                user.memberId());
        return ApiResponse.success(SuccessCode.RESERVATION_FOUND, response);
    }

    @Operation(
            summary = "현상소 예약 취소",
            description = "예약을 취소합니다."
    )
    @DeleteMapping("/{photoLabId}/reservations/{reservationId}")
    public ApiResponse<ReservationResponse.Cancel> cancelReservation(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long photoLabId,
            @PathVariable Long reservationId
    ) {
        ReservationResponse.Cancel response =  reservationCommandService.cancelReservation(photoLabId, reservationId, user.memberId());
        return ApiResponse.success(SuccessCode.RESERVATION_CANCELLED,response);
    }
}
