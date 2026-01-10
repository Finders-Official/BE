package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabBusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface PhotoLabBusinessHourRepository extends JpaRepository<PhotoLabBusinessHour, Long> {

    Optional<PhotoLabBusinessHour> findByPhotoLabIdAndDayOfWeek(Long photoLabId, DayOfWeek dayOfWeek);
}
