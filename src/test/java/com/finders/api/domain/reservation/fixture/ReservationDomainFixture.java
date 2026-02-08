package com.finders.api.domain.reservation.fixture;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.reservation.dto.ReservationRequest;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.entity.ReservationSlot;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.util.EntityTestFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.finders.api.domain.reservation.policy.ReservationPolicy.DEFAULT_MAX_CAPACITY;

public final class ReservationDomainFixture {

    private ReservationDomainFixture() {}

    public static MemberUser memberUser(long memberId) {
        MemberUser user = EntityTestFactory.newInstance(MemberUser.class);
        ReflectionTestUtils.setField(user, "id", memberId);
        return user;
    }

    public static PhotoLab photoLab(long photoLabId) {
        PhotoLab lab = EntityTestFactory.newInstance(PhotoLab.class);
        ReflectionTestUtils.setField(lab, "id", photoLabId);
        return lab;
    }

    public static Reservation reserved(
            long reservationId,
            MemberUser user,
            ReservationSlot slot,
            PhotoLab lab,
            ReservationRequest.Create req
    ) {
        Reservation r = Reservation.reserve(user, slot, lab, req);
        ReflectionTestUtils.setField(r, "id", reservationId);
        return r;
    }

    public static Reservation canceled(
            long reservationId,
            MemberUser user,
            ReservationSlot slot,
            PhotoLab lab,
            ReservationRequest.Create req
    ) {
        Reservation r = reserved(reservationId, user, slot, lab, req);
        r.cancel();
        return r;
    }


    public static ReservationSlot slot(PhotoLab lab, long slotId, LocalDate date, LocalTime time) {
        ReservationSlot slot = ReservationSlot.create(lab, date, time, DEFAULT_MAX_CAPACITY);
        ReflectionTestUtils.setField(slot, "id", slotId);
        return slot;
    }

    public static Reservation reservation(long reservationId,
                                          MemberUser user,
                                          ReservationSlot slot,
                                          PhotoLab lab,
                                          ReservationRequest.Create req) {
        Reservation r = Reservation.reserve(user, slot, lab, req);
        ReflectionTestUtils.setField(r, "id", reservationId);
        return r;
    }
}
