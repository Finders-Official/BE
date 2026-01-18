package com.finders.api.domain.photo.service;

import com.finders.api.domain.photo.dto.ShareResponse;
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
 * 사진 복원 공유 서비스
 * <p>
 * AI 복원 이미지를 커뮤니티에 공유하는 기능을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoRestorationShareService {

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;

    /**
     * AI 복원 이미지를 커뮤니티 공유용으로 Public 버킷에 복사
     * <p>
     * Private 버킷의 복원 완료 이미지를 Public 버킷(temp/)으로 복사하여
     * 커뮤니티 게시글 작성에 사용할 수 있도록 합니다.
     * <p>
     * 트랜잭션 없음: DB 변경이 없고 조회 + GCS 작업만 수행
     *
     * @param memberId      회원 ID
     * @param restorationId 복원 ID
     * @return objectPath, width, height 정보
     */
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
                restoration.getRestoredUrl(),
                StoragePath.TEMP_PUBLIC,  // temp/{memberId}/{uuid}.png
                memberId,
                "shared.png"
        );

        log.info("[PhotoRestorationShareService.shareToPublic] Shared: restorationId={}, publicPath={}",
                restorationId, publicObjectPath);

        // 4. objectPath + 메타데이터 반환
        return ShareResponse.of(
                publicObjectPath,
                restoration.getRestoredWidth(),
                restoration.getRestoredHeight()
        );
    }
}
