package com.finders.api.domain.member.dto;

import java.time.LocalDateTime;

public record VerifiedPhoneInfo(
        String phone,
        LocalDateTime expiryTime
) {
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
