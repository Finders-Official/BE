package com.finders.api.domain.reservation.policy;

/**
 * 예약 도메인 정책 상수
 */
public final class ReservationPolicy {

    private ReservationPolicy() {}

    /** 예약 시간 간격 (분) */
    public static final int TIME_INTERVAL_MINUTES = 60;

    /** 슬롯 최대 수용 인원 */
    public static final int DEFAULT_MAX_CAPACITY = 3;
}
