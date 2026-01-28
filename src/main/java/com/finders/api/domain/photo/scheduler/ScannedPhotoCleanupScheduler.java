package com.finders.api.domain.photo.scheduler;

import com.finders.api.domain.photo.service.command.ScannedPhotoCleanupService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScannedPhotoCleanupScheduler {

    private final ScannedPhotoCleanupService service;

    @Scheduled(cron = "0 0 * * * ?")
    public void run() {
        log.info("[ScannedPhotoCleanupScheduler.run] 만료된 스캔 사진 삭제 작업을 시작합니다.");
        int deletedCount = service.cleanupExpired(LocalDateTime.now());
        log.info("[ScannedPhotoCleanupScheduler.run] 작업 완료. 삭제된 사진 수: {}", deletedCount);
    }
}
