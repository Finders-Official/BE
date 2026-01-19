package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.repository.PhotoRestorationRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 사진 복원 Query 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoRestorationQueryServiceImpl implements PhotoRestorationQueryService {

    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final PhotoRestorationRepository restorationRepository;
    private final StorageService storageService;

    @Override
    public RestorationResponse.Detail getRestoration(Long memberId, Long restorationId) {
        PhotoRestoration restoration = restorationRepository.findById(restorationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));

        if (!restoration.isOwner(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String originalSignedUrl = storageService.getSignedUrl(
                restoration.getOriginalPath(), SIGNED_URL_EXPIRY_MINUTES).url();

        String restoredSignedUrl = null;
        if (restoration.getRestoredPath() != null) {
            restoredSignedUrl = storageService.getSignedUrl(
                    restoration.getRestoredPath(), SIGNED_URL_EXPIRY_MINUTES).url();
        }

        return RestorationResponse.Detail.from(restoration, originalSignedUrl, restoredSignedUrl);
    }

    @Override
    public Page<RestorationResponse.Summary> getRestorationHistory(Long memberId, Pageable pageable) {
        Page<PhotoRestoration> restorations = restorationRepository.findByMemberId(memberId, pageable);

        // 1. 모든 썸네일 경로 수집
        List<String> thumbnailPaths = restorations.getContent().stream()
                .map(this::getThumbnailPath)
                .toList();

        // 2. 배치로 Signed URL 생성 (N+1 쿼리 방지)
        Map<String, StorageResponse.SignedUrl> signedUrlMap = storageService.getSignedUrls(
                thumbnailPaths,
                SIGNED_URL_EXPIRY_MINUTES
        );

        // 3. Response 변환
        return restorations.map(restoration -> {
            String thumbnailPath = getThumbnailPath(restoration);
            String signedUrl = signedUrlMap.get(thumbnailPath).url();
            return RestorationResponse.Summary.from(restoration, signedUrl);
        });
    }

    /**
     * 썸네일 경로 결정 (복원된 이미지 우선, 없으면 원본)
     */
    private String getThumbnailPath(PhotoRestoration restoration) {
        return restoration.getRestoredPath() != null
                ? restoration.getRestoredPath()
                : restoration.getOriginalPath();
    }
}
