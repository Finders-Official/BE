package com.finders.api.domain.store.dto.request;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PhotoLabSearchCondition(
        Long memberId,
        String query,
        List<Long> tagIds,
        Long regionId,
        LocalDate date,
        Integer page,
        Integer size,
        Double lat,
        Double lng
) {
}
