package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.response.ShareResponse;
import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.repository.PhotoRestorationRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사진 복원 공유 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoRestorationShareServiceImpl implements PhotoRestorationShareService {

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;

    @Override
    public ShareResponse shareToPublic(Long memberId, Long restorationId) {
        PhotoRestoration restoration = restorationRepository.findById(restorationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));

        // 1. 권한 검증
        if (!restoration.isOwner(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 2. 완료 상태 검증
        if (!restoration.isCompleted()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "완료된 복원만 공유할 수 있습니다.");
        }

        // 3. Private → Public 버킷 복사 (GCS 내부 복사, 빠르고 비용 없음)
        String publicObjectPath = storageService.copyToPublic(
                restoration.getRestoredPath(),
                StoragePath.TEMP_PUBLIC,  // temp/{memberId}/{uuid}.png
                memberId,
                "shared.png"
        );

        log.info("[PhotoRestorationShareServiceImpl.shareToPublic] Shared: restorationId={}, publicPath={}",
                restorationId, publicObjectPath);

        // 4. objectPath + 메타데이터 반환
        return ShareResponse.of(
                publicObjectPath,
                restoration.getRestoredWidth(),
                restoration.getRestoredHeight()
        );
    }
}
