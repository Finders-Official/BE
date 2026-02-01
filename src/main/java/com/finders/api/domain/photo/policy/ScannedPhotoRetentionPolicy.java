package com.finders.api.domain.photo.policy;

import java.time.LocalDateTime;

public class ScannedPhotoRetentionPolicy {

    private static final int RETENTION_DAYS = 30;

    public static LocalDateTime expiredAt(LocalDateTime now) {
        return now.minusDays(RETENTION_DAYS);
    }
}
