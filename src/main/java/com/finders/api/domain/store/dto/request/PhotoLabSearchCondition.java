package com.finders.api.domain.store.dto.request;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalTime;

@Builder
public record PhotoLabSearchCondition(
        Long memberId,
        String query,
        List<Long> tagIds,
        Long parentRegionId,
        List<Long> regionIds,
        LocalDate date,
        List<LocalTime> times,
        Integer page,
        Integer size,
        Double lat,
        Double lng
) {
}
