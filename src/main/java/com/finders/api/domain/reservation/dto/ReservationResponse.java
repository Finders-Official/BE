package com.finders.api.domain.reservation.dto;

import com.finders.api.domain.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationResponse {

    /**
     * 예약 가능 시간 응답
     */
    public record AvailableTimes(
            Long storeId,
            LocalDate reservationDate,
            List<LocalTime> availableTimes
    ) {
        public static AvailableTimes of(
                Long storeId,
                LocalDate reservationDate,
                List<LocalTime> availableTimes
        ) {
            return new AvailableTimes(storeId, reservationDate, availableTimes);
        }
    }

    /**
     * 예약 생성 응답
     */
    public record Created(
            Long reservationId
    ) {
        public static Created of(Long reservationId) {
            return new Created(reservationId);
        }
    }

    /**
     * 예약 상세 응답
     */
    @lombok.Builder
    public record Detail(
            Long reservationId,
            String storeName,
            LocalDate reservationDate,
            LocalTime reservationTime,
            List<String> taskTypes,
            Integer filmCount,
            String memo,
            String address,
            String addressDetail
    ) {
        public static Detail from(Reservation reservation) {
            List<String> taskTypes = new ArrayList<>();
            if (reservation.isDevelop()) taskTypes.add("DEVELOP");
            if (reservation.isScan()) taskTypes.add("SCAN");
            if (reservation.isPrint()) taskTypes.add("PRINT");

            return Detail.builder()
                    .reservationId(reservation.getId())
                    .storeName(reservation.getPhotoLab().getName())
                    .reservationDate(reservation.getSlot().getReservationDate())
                    .reservationTime(reservation.getSlot().getReservationTime())
                    .taskTypes(taskTypes)
                    .filmCount(reservation.getRollCount())
                    .memo(reservation.getRequestMessage())
                    .address(reservation.getPhotoLab().getAddress())
                    .addressDetail(reservation.getPhotoLab().getAddressDetail())
                    .build();
        }
    }

    /**
     * 예약 취소 응답
     */
    public record Cancel(
            Long photoLabId
    ) {
        public static Cancel of(Long photoLabId) {
            return new Cancel(photoLabId);
        }
    }
}
