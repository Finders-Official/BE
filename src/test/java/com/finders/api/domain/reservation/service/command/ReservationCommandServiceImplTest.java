package com.finders.api.domain.reservation.service.command;

import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.reservation.dto.ReservationRequest;
import com.finders.api.domain.reservation.dto.ReservationResponse;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.entity.ReservationSlot;
import com.finders.api.domain.reservation.fixture.ReservationDomainFixture;
import com.finders.api.domain.reservation.fixture.ReservationRequestFixture;
import com.finders.api.domain.reservation.repository.ReservationRepository;
import com.finders.api.domain.reservation.repository.ReservationSlotRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCommandServiceImplTest {

    @Mock PhotoLabRepository photoLabRepository;
    @Mock MemberUserRepository memberUserRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock
    ReservationSlotRepository reservationSlotRepository;

    @InjectMocks
    ReservationCommandServiceImpl service;

    @Test
    void createReservation_정상_슬롯없으면_생성후_예약저장() {
        long labId = 1L;
        long memberId = 10L;

        var lab = ReservationDomainFixture.photoLab(labId);
        var user = ReservationDomainFixture.memberUser(memberId);

        LocalDate date = LocalDate.of(2026, 2, 4);
        LocalTime time = LocalTime.of(10, 0);
        ReservationRequest.Create req = ReservationRequestFixture.create(date, time);  //요청형태 생성

        // 슬롯이 없어서 orElseGet 진입 -> save(slot) -> 다시 findForUpdate
        var createdSlot = ReservationDomainFixture.slot(lab, 100L, date, time);

        given(photoLabRepository.findById(labId)).willReturn(Optional.of(lab));
        given(memberUserRepository.findById(memberId)).willReturn(Optional.of(user));

        given(reservationSlotRepository.findByPhotoLabIdAndReservationDateAndReservationTimeForUpdate(labId, date, time))
                .willReturn(Optional.empty()) // 1차 조회: 없음
                .willReturn(Optional.of(createdSlot)); // 2차 조회: 생성된 슬롯 조회됨

        given(reservationSlotRepository.save(any(ReservationSlot.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // reservationRepository.save()가 반환하는 Reservation에 id가 있어야 response가 만들어짐
        given(reservationRepository.save(any(Reservation.class)))
                .willAnswer(inv -> {
                    Reservation r = inv.getArgument(0);
                    // id 강제 주입
                    org.springframework.test.util.ReflectionTestUtils.setField(r, "id", 777L);
                    return r;
                });


        ReservationResponse.Created response = service.createReservation(labId, memberId, req);

        assertThat(response.reservationId()).isEqualTo(777L); // 필드명 다르면 수정

        then(reservationSlotRepository).should().save(any(ReservationSlot.class));
        then(reservationRepository).should().save(any(Reservation.class));
    }

    @Test
    void createReservation_현상소없으면_STORE_NOT_FOUND() {
        long labId = 1L;
        long memberId = 10L;

        given(photoLabRepository.findById(labId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReservation(labId, memberId, ReservationRequestFixture.defaultCreate()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND));
    }

    @Test
    void createReservation_회원없으면_MEMBER_NOT_FOUND() {
        long labId = 1L;
        long memberId = 10L;

        var lab = ReservationDomainFixture.photoLab(labId);

        given(photoLabRepository.findById(labId)).willReturn(Optional.of(lab));
        given(memberUserRepository.findById(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReservation(labId, memberId, ReservationRequestFixture.defaultCreate()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void cancelReservation_이미취소된예약이면_슬롯에_아무작업도하지않는다() {
        long labId = 1L;
        long memberId = 10L;
        long reservationId = 5L;

        var lab = ReservationDomainFixture.photoLab(labId);
        var user = ReservationDomainFixture.memberUser(memberId);
        var slot = ReservationDomainFixture.slot(lab, 100L, LocalDate.of(2026,2,4), LocalTime.of(10,0));

        // reservedCount를 1로 올려둔다
        slot.increaseReservedCountOrThrow();

        var req = ReservationRequestFixture.defaultCreate();
        var reservation = ReservationDomainFixture.reserved(reservationId,user, slot, lab,req);
        reservation.cancel(); // 이미 취소 상태로

        given(reservationRepository.findByIdAndPhotoLabIdAndUserIdForUpdate(reservationId, labId, memberId))
                .willReturn(Optional.of(reservation));

        ReservationResponse.Cancel response = service.cancelReservation(labId, reservationId, memberId);

        assertThat(response).isNotNull();

        // 이미 취소면 slotRepository.findByIdForUpdate를 호출하지 않아야 함
        then(reservationSlotRepository).shouldHaveNoInteractions();
    }
}
