package com.finders.api.domain.store.service;

import com.finders.api.domain.store.dto.response.PhotoLabDocumentResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.entity.PhotoLabDocument;
import com.finders.api.domain.store.enums.DocumentType;
import com.finders.api.domain.store.repository.PhotoLabDocumentRepository;
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
public class PhotoLabDocumentService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabDocumentRepository photoLabDocumentRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public StorageResponse.PresignedUrl createDocumentPresignedUrl(
            Long ownerId,
            Long photoLabId,
            DocumentType documentType,
            String fileName
    ) {
        log.info("[PhotoLabDocumentService.createDocumentPresignedUrl] photoLabId={}, documentType={}",
                photoLabId, documentType);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (ownerId == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        validateFileName(fileName);

        String documentTypeSegment = documentType.name().toLowerCase();
        return storageService.generatePresignedUrl(
                StoragePath.LAB_DOCUMENT,
                photoLabId,
                documentTypeSegment,
                fileName
        );
    }

    @Transactional
    public PhotoLabDocumentResponse.Create registerDocument(
            Long ownerId,
            Long photoLabId,
            DocumentType documentType,
            String objectPath,
            String fileName
    ) {
        log.info("[PhotoLabDocumentService.registerDocument] photoLabId={}, documentType={}",
                photoLabId, documentType);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (ownerId == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        validateFileName(fileName);
        validateDocumentObjectPath(photoLabId, documentType, objectPath);

        PhotoLabDocument document = PhotoLabDocument.builder()
                .photoLab(photoLab)
                .documentType(documentType)
                .objectPath(objectPath)
                .fileName(fileName)
                .build();

        photoLabDocumentRepository.save(document);

        return PhotoLabDocumentResponse.Create.from(document);
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "fileName은 필수입니다.");
        }
    }

    private void validateDocumentObjectPath(Long photoLabId, DocumentType documentType, String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "objectPath는 필수입니다.");
        }

        String documentTypeSegment = documentType.name().toLowerCase();
        String prefix = String.format("photo-labs/%d/documents/%s/", photoLabId, documentTypeSegment);
        if (!objectPath.startsWith(prefix)) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_PATH, "잘못된 문서 경로입니다.");
        }
    }

}

