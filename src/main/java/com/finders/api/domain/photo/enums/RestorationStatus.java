package com.finders.api.domain.photo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 복원 상태
 */
@Getter
@RequiredArgsConstructor
public enum RestorationStatus {
    PENDING("대기 중"),
    PROCESSING("처리 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;
}
