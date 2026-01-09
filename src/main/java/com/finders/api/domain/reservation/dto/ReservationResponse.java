package com.finders.api.domain.reservation.dto;

import com.finders.api.domain.reservation.entity.Reservation;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationResponse {

    @Builder
    public record AvailableTimes(
            Long storeId,
            LocalDate reservationDate,
            List<LocalTime> availableTimes
    ) {}

    @Builder
    public record Created(
            Long reservationId
    ) {}

    @Builder
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

    @Builder
    public record Cancel(
            Long photoLabId
    ) {}
}
