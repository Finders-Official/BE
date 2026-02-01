package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.policy.ScannedPhotoRetentionPolicy;
import com.finders.api.domain.photo.repository.ScannedPhotoRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScannedPhotoCleanupService {

    private final ScannedPhotoRepository repository;

    @Transactional
    public int cleanupExpired(LocalDateTime now) {
        LocalDateTime expiredAt =
                ScannedPhotoRetentionPolicy.expiredAt(now);

        return repository.deleteExpired(expiredAt);
    }
}
