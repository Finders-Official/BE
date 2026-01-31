package com.finders.api.domain.reservation.calculator;

import com.finders.api.domain.store.entity.PhotoLabBusinessHour;
import com.finders.api.domain.store.repository.PhotoLabBusinessHourRepository;
import java.time.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessHourCalculator {

    private final PhotoLabBusinessHourRepository photoLabBusinessHourRepository;

    public LocalDateTime calculateCompletedAt(
            Long photoLabId,
            LocalDateTime startAt,
            int requiredMinutes
    ) {
        if (requiredMinutes <= 0) {
            return adjustToBusinessTime(photoLabId, startAt);
        }

        LocalDateTime current = adjustToBusinessTime(photoLabId, startAt);
        int remain = requiredMinutes;

        while (remain > 0) {
            BusinessWindow window = getBusinessWindow(photoLabId, current.toLocalDate());

            if (window == null) {
                current = nextBusinessOpen(photoLabId, current.toLocalDate().plusDays(1));
                continue;
            }

            long available = Duration.between(current, window.close).toMinutes();
            if (available >= remain) {
                return current.plusMinutes(remain);
            }

            remain -= (int) available;
            current = nextBusinessOpen(photoLabId, current.toLocalDate().plusDays(1));
        }

        return current;
    }

    private LocalDateTime adjustToBusinessTime(Long photoLabId, LocalDateTime t) {
        LocalDate date = t.toLocalDate();

        while (true) {
            BusinessWindow window = getBusinessWindow(photoLabId, date);
            if (window == null) {
                date = date.plusDays(1);
                t = LocalDateTime.of(date, LocalTime.MIDNIGHT);
                continue;
            }

            if (t.isBefore(window.open)) return window.open;
            if (!t.isBefore(window.close)) {
                date = date.plusDays(1);
                t = LocalDateTime.of(date, LocalTime.MIDNIGHT);
                continue;
            }

            return t;
        }
    }

    private LocalDateTime nextBusinessOpen(Long photoLabId, LocalDate date) {
        LocalDate d = date;
        while (true) {
            BusinessWindow window = getBusinessWindow(photoLabId, d);
            if (window != null) return window.open;
            d = d.plusDays(1);
        }
    }

    private BusinessWindow getBusinessWindow(Long photoLabId, LocalDate date) {
        Optional<PhotoLabBusinessHour> opt =
                photoLabBusinessHourRepository
                        .findByPhotoLabIdAndDayOfWeek(photoLabId, date.getDayOfWeek());

        if (opt.isEmpty()) return null;

        PhotoLabBusinessHour bh = opt.get();
        if (bh.isClosed()) return null;

        LocalDateTime open = date.atTime(bh.getOpenTime());
        LocalDateTime close = date.atTime(bh.getCloseTime());

        if (!open.isBefore(close)) return null;

        return new BusinessWindow(open, close);
    }

    private static class BusinessWindow {
        final LocalDateTime open;
        final LocalDateTime close;

        BusinessWindow(LocalDateTime open, LocalDateTime close) {
            this.open = open;
            this.close = close;
        }
    }
}
