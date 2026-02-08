package com.finders.api.domain.reservation.fixture;

import com.finders.api.domain.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ReservationRequestFixture {

    private ReservationRequestFixture() {}

    public static ReservationRequest.Create create(
            LocalDate date,
            LocalTime time
    ) {
        ReservationRequest.Create base = defaultCreate();
        return new ReservationRequest.Create(
                date,
                time,
                base.taskTypes(),
                base.filmCount(),
                base.memo()
        );
    }


    /** 가장 자주 쓰는 기본 요청 */
    public static ReservationRequest.Create defaultCreate() {
        return new ReservationRequest.Create(
                LocalDate.of(2026, 2, 4),
                LocalTime.of(10, 0),
                List.of("DEVELOP", "SCAN"),
                1,
                "default memo"
        );
    }

    /** 작업 유형만 바꾸고 싶을 때 (DEVELOP/SCAN/PRINT 조합) */
    public static ReservationRequest.Create createWithTaskTypes(List<String> taskTypes) {
        ReservationRequest.Create base = defaultCreate();
        return new ReservationRequest.Create(
                base.reservationDate(),
                base.reservationTime(),
                taskTypes,
                base.filmCount(),
                base.memo()
        );
    }


    /** 조합 프리셋: DEVELOP+SCAN */
    public static ReservationRequest.Create developAndScan() {
        return createWithTaskTypes(List.of("DEVELOP", "SCAN"));
    }

    /** 조합 프리셋: DEVELOP+SCAN+PRINT */
    public static ReservationRequest.Create allTasks() {
        return createWithTaskTypes(List.of("DEVELOP", "SCAN", "PRINT"));
    }
}
