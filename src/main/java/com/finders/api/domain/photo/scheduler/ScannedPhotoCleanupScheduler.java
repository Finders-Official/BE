package com.finders.api.domain.photo.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScannedPhotoCleanupScheduler {

    private final ScannedPhotoCleanupService service;

    @Scheduled(cron = "0 0 0 * * ?")
    public void run() {
        int deleted = service.cleanupExpired(LocalDateTime.now());
    }
}
