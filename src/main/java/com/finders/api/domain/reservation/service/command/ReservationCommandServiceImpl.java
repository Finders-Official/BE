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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.finders.api.domain.reservation.policy.ReservationPolicy.DEFAULT_MAX_CAPACITY;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final MemberUserRepository memberUserRepository;
    private final PhotoLabRepository photoLabRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    @Override
    public ReservationResponse.Created createReservation(Long photoLabId, Long memberId, ReservationRequest.Create request) {

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

        return ReservationResponse.Created.builder()
                .reservationId(saved.getId())
                .build();
    }

    @Override
    public ReservationResponse.Cancel cancelReservation(Long photoLabId, Long reservationId, Long memberId) {

        Reservation reservation = reservationRepository.findByIdAndPhotoLabIdAndUserId(memberId,reservationId, photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationResponse.Cancel.builder()
                    .photoLabId(photoLabId)
                    .build();
        }

        reservation.cancel();

        ReservationSlot lockedSlot = reservationSlotRepository.findByIdForUpdate(reservation.getSlot().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_SLOT_NOT_FOUND));

        lockedSlot.decreaseReservedCountSafely();

        return ReservationResponse.Cancel.builder()
                .photoLabId(photoLabId)
                .build();
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
                    }

                    return reservationSlotRepository
                            .findByPhotoLabIdAndReservationDateAndReservationTimeForUpdate(photoLab.getId(), date, time)
                            .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_SLOT_NOT_FOUND));
                });
    }
}
