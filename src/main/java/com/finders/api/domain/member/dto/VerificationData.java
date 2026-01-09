package com.finders.api.domain.member.dto;

import java.time.LocalDateTime;

public record VerificationData(
        String phone,
        String code,
        LocalDateTime expiryTime
) {
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
