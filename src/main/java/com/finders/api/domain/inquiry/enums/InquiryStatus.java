package com.finders.api.domain.inquiry.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {
    PENDING("답변 대기"),
    ANSWERED("답변 완료"),
    CLOSED("종료");

    private final String description;
}
