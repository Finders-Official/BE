package com.finders.api.domain.store.service;

import com.finders.api.domain.store.dto.response.PhotoLabImageResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.entity.PhotoLabImage;
import com.finders.api.domain.store.repository.PhotoLabImageRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabImageService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabImageRepository photoLabImageRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public StorageResponse.PresignedUrl createImagePresignedUrl(
            Long ownerId,
            Long photoLabId,
            String fileName
    ) {
        log.info("[PhotoLabImageService.createImagePresignedUrl] photoLabId={}", photoLabId);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (ownerId == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        validateFileName(fileName);

        return storageService.generatePresignedUrl(
                StoragePath.LAB_IMAGE,
                photoLabId,
                fileName
        );
    }

    @Transactional
    public PhotoLabImageResponse.Create registerImage(
            Long ownerId,
            Long photoLabId,
            String objectPath,
            Integer displayOrder,
            Boolean isMain
    ) {
        log.info("[PhotoLabImageService.registerImage] photoLabId={}, displayOrder={}, isMain={}",
                photoLabId, displayOrder, isMain);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (ownerId == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        validateImageObjectPath(photoLabId, objectPath);
        validateDisplayOrder(displayOrder);

        Integer resolvedDisplayOrder = resolveDisplayOrder(photoLabId, displayOrder);

        if (Boolean.TRUE.equals(isMain)) {
            photoLabImageRepository.clearMainByPhotoLabId(photoLabId);
        }

        PhotoLabImage photoLabImage = PhotoLabImage.builder()
                .photoLab(photoLab)
                .objectPath(objectPath)
                .displayOrder(resolvedDisplayOrder)
                .isMain(isMain)
                .build();

        photoLabImageRepository.save(photoLabImage);

        return PhotoLabImageResponse.Create.from(photoLabImage);
    }

    private Integer resolveDisplayOrder(Long photoLabId, Integer displayOrder) {
        if (displayOrder != null) {
            return displayOrder;
        }

        Integer maxDisplayOrder = photoLabImageRepository.findMaxDisplayOrderByPhotoLabId(photoLabId);
        return maxDisplayOrder + 1;
    }

    private void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder != null && displayOrder < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "displayOrder must be >= 0.");
        }
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "fileName은 필수입니다.");
        }
    }

    private void validateImageObjectPath(Long photoLabId, String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "objectPath는 필수입니다.");
        }

        String prefix = String.format("photo-labs/%d/images/", photoLabId);
        if (!objectPath.startsWith(prefix)) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_PATH, "잘못된 이미지 경로입니다.");
        }
    }
}
