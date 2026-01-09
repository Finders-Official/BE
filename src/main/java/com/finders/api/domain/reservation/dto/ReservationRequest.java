package com.finders.api.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationRequest {

    public record Create(

            @Schema(example = "2026-01-15", description = "예약 날짜 (yyyy-MM-dd)")
            @NotNull(message = "예약 날짜는 필수입니다.")
            LocalDate reservationDate,

            @Schema(example = "14:00", description = "예약 시간 (HH:mm)")
            @NotNull(message = "예약 시간은 필수입니다.")
            LocalTime reservationTime,

            @Schema(example = "[\"DEVELOP\",\"SCAN\"]", description = "작업 유형 목록 (DEVELOP/SCAN/PRINT)")
            @NotEmpty(message = "작업 유형은 최소 1개 이상 선택해야 합니다.")
            List<String> taskTypes,

            @Schema(example = "3", description = "필름(롤) 수")
            @NotNull(message = "필름 수는 필수입니다.")
            @Min(value = 1, message = "필름 수는 최소 1개 이상이어야 합니다.")
            Integer filmCount,

            @Schema(example = "스캔 해상도 높게 부탁드려요", description = "요청사항(선택, 500자 이내)")
            @Size(max = 500, message = "요청사항은 500자 이내로 작성해주세요.")
            String memo
    ) {}
}
