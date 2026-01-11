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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabDocumentService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabDocumentRepository photoLabDocumentRepository;
    private final StorageService storageService;

    @Transactional
    public PhotoLabDocumentResponse.Create uploadDocument(
            Long ownerId,
            Long photoLabId,
            DocumentType documentType,
            MultipartFile file
    ) {
        log.info("[PhotoLabDocumentService.uploadDocument] photoLabId={}, documentType={}",
                photoLabId, documentType);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (ownerId == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        validateFile(file);

        StorageResponse.Upload upload = storageService.uploadPrivate(
                file,
                StoragePath.LAB_DOCUMENT,
                photoLabId,
                documentType.name().toLowerCase()
        );

        PhotoLabDocument document = PhotoLabDocument.builder()
                .photoLab(photoLab)
                .documentType(documentType)
                .fileUrl(upload.objectPath())
                .fileName(file.getOriginalFilename())
                .build();

        photoLabDocumentRepository.save(document);

        return PhotoLabDocumentResponse.Create.from(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "file is required.");
        }
    }
}

