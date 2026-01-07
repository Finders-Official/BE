package com.finders.api.domain.photo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 복원 피드백 평가
 */
@Getter
@RequiredArgsConstructor
public enum FeedbackRating {
    GOOD("좋음"),
    BAD("나쁨");

    private final String description;
}
