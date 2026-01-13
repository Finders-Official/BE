package com.finders.api.domain.reservation.enums;

public enum ReservationStatus {
    RESERVED,  // 예약이 유효하고, 아직 종료되지 않음
    COMPLETED, // 예약이 정상적으로 종료됨
    CANCELED  // 예약이 취소됨 (노쇼 포함)
}
