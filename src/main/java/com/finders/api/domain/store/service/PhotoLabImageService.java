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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabImageService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabImageRepository photoLabImageRepository;
    private final StorageService storageService;

    @Transactional
    public PhotoLabImageResponse.Create uploadImage(
            Long ownerId,
            Long photoLabId,
            MultipartFile file,
            Integer displayOrder,
            Boolean isMain
    ) {
        log.info("[PhotoLabImageService.uploadImage] photoLabId={}, displayOrder={}, isMain={}",
                photoLabId, displayOrder, isMain);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (ownerId == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        validateFile(file);
        validateDisplayOrder(displayOrder);

        Integer resolvedDisplayOrder = resolveDisplayOrder(photoLabId, displayOrder);

        StorageResponse.Upload upload = storageService.uploadPublic(
                file,
                StoragePath.LAB_IMAGE,
                photoLabId
        );

        if (Boolean.TRUE.equals(isMain)) {
            photoLabImageRepository.clearMainByPhotoLabId(photoLabId);
        }

        PhotoLabImage photoLabImage = PhotoLabImage.builder()
                .photoLab(photoLab)
                .imageUrl(upload.objectPath())
                .displayOrder(resolvedDisplayOrder)
                .isMain(isMain)
                .build();

        photoLabImageRepository.save(photoLabImage);

        String publicUrl = upload.url() != null
                ? upload.url()
                : storageService.getPublicUrl(photoLabImage.getImageUrl());

        return PhotoLabImageResponse.Create.from(photoLabImage, publicUrl);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "file is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(
                    ErrorCode.STORAGE_INVALID_FILE_TYPE,
                    "Only image/* content types are allowed."
            );
        }
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
}
