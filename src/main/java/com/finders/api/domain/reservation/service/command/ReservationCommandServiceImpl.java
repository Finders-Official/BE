package com.finders.api.domain.reservation.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.reservation.dto.ReservationRequest;
import com.finders.api.domain.reservation.dto.ReservationResponse;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.entity.ReservationSlot;
import com.finders.api.domain.reservation.enums.ReservationStatus;
import com.finders.api.domain.reservation.repository.ReservationRepository;
import com.finders.api.domain.reservation.repository.ReservationSlotRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.finders.api.domain.reservation.policy.ReservationPolicy.DEFAULT_MAX_CAPACITY;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final MemberUserRepository memberUserRepository;
    private final PhotoLabRepository photoLabRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    @Override
    @CacheEvict(value = "popularPhotoLabs", key = "'top8'")
    public ReservationResponse.Created createReservation(Long photoLabId, Long memberId, ReservationRequest.Create request) {

        log.info("[ReservationCommandServiceImpl.createReservation] photoLabId: {}, memberId: {}", photoLabId, memberId);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        MemberUser user = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        ReservationSlot slot = getOrCreateSlotWithLock(
                photoLab,
                request.reservationDate(),
                request.reservationTime()
        );

        // 정원 초과 방지 + 증가
        slot.increaseReservedCountOrThrow();

        Reservation reservation = Reservation.reserve(user, slot, photoLab, request);
        Reservation saved = reservationRepository.save(reservation);

        return ReservationResponse.Created.of(saved.getId());
    }

    @Override
    @CacheEvict(value = "popularPhotoLabs", key = "'top8'")
    public ReservationResponse.Cancel cancelReservation(Long photoLabId, Long reservationId, Long memberId) {

        Reservation reservation = reservationRepository.findByIdAndPhotoLabIdAndUserId(reservationId, photoLabId,memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationResponse.Cancel.of(photoLabId);
        }

        reservation.cancel();

        ReservationSlot lockedSlot = reservationSlotRepository.findByIdForUpdate(reservation.getSlot().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_SLOT_NOT_FOUND));

        lockedSlot.decreaseReservedCountSafely();

        return ReservationResponse.Cancel.of(photoLabId);
    }

    private ReservationSlot getOrCreateSlotWithLock(PhotoLab photoLab, LocalDate date, LocalTime time) {

        return reservationSlotRepository
                .findByPhotoLabIdAndReservationDateAndReservationTimeForUpdate(photoLab.getId(), date, time)
                .orElseGet(() -> {
                    try {
                        ReservationSlot slot = ReservationSlot.create(
                                photoLab,
                                date,
                                time,
                                DEFAULT_MAX_CAPACITY
                        );
                        reservationSlotRepository.save(slot);
                    } catch (DataIntegrityViolationException ignored) {
                        // 동시성 이슈로 이미 동일한 슬롯이 생성된 경우
                        log.debug(
                                "ReservationSlot already exists due to concurrent creation. photoLabId={}, date={}, time={}",
                                photoLab.getId(), date, time
                        );
                    }

                    return reservationSlotRepository
                            .findByPhotoLabIdAndReservationDateAndReservationTimeForUpdate(photoLab.getId(), date, time)
                            .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_SLOT_NOT_FOUND));
                });
    }
}
