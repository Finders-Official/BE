package com.finders.api.domain.reservation.service.query;

import com.finders.api.domain.reservation.dto.ReservationResponse;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.entity.ReservationSlot;
import com.finders.api.domain.reservation.repository.ReservationRepository;
import com.finders.api.domain.reservation.repository.ReservationSlotRepository;
import com.finders.api.domain.store.entity.PhotoLabBusinessHour;
import com.finders.api.domain.store.repository.PhotoLabBusinessHourRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.finders.api.domain.reservation.policy.ReservationPolicy.TIME_INTERVAL_MINUTES;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabBusinessHourRepository photoLabBusinessHourRepository;

    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public ReservationResponse.AvailableTimes getAvailableTimes(Long photoLabId, LocalDate date) {

        // 현상소 존재 확인
        photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 요일별 영업시간 조회
        DayOfWeek dow = date.getDayOfWeek();
        PhotoLabBusinessHour bh = photoLabBusinessHourRepository
                .findByPhotoLabIdAndDayOfWeek(photoLabId, dow)
                .orElseThrow(() -> new CustomException(ErrorCode.BUSINESS_HOUR_NOT_FOUND));

        // 휴무면 빈 리스트
        if (bh.isClosed()) {
            return ReservationResponse.AvailableTimes.builder()
                    .storeId(photoLabId)
                    .reservationDate(date)
                    .availableTimes(List.of())
                    .build();
        }

        // 1) 영업시간 기반 후보 시간 생성
        List<LocalTime> candidateTimes = generateTimes(
                bh.getOpenTime(),
                bh.getCloseTime(),
                TIME_INTERVAL_MINUTES
        );

        // 2) 해당 날짜 슬롯 중 "꽉 찬 시간" 제외
        List<ReservationSlot> slots =
                reservationSlotRepository.findByPhotoLabIdAndReservationDate(photoLabId, date);

        Set<LocalTime> fullyBookedTimes = slots.stream()
                .filter(s -> s.getReservedCount() >= s.getMaxCapacity())
                .map(ReservationSlot::getReservationTime)
                .collect(Collectors.toSet());

        List<LocalTime> availableTimes = candidateTimes.stream()
                .filter(t -> !fullyBookedTimes.contains(t))
                .toList();

        return ReservationResponse.AvailableTimes.builder()
                .storeId(photoLabId)
                .reservationDate(date)
                .availableTimes(availableTimes)
                .build();
    }

    @Override
    public ReservationResponse.Detail getReservation(Long photoLabId, Long reservationId, Long memberId) {

        Reservation reservation = reservationRepository
                .findDetailByIdAndPhotoLabIdAndMemberId(reservationId, photoLabId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        return ReservationResponse.Detail.from(reservation);
    }


    private List<LocalTime> generateTimes(LocalTime open, LocalTime close, int intervalMinutes) {
        List<LocalTime> times = new ArrayList<>();
        LocalTime t = open;

        while (!t.plusMinutes(intervalMinutes).isAfter(close)) {
            times.add(t);
            t = t.plusMinutes(intervalMinutes);
        }
        return times;
    }
}
