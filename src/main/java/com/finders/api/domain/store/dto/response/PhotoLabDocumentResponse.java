package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.entity.PhotoLabDocument;
import com.finders.api.domain.store.enums.DocumentType;
import lombok.Builder;

public class PhotoLabDocumentResponse {

    @Builder
    public record Create(
            Long id,
            Long photoLabId,
            DocumentType documentType,
            String documentUrl,
            String fileName
    ) {
        public static Create from(PhotoLabDocument document) {
            return Create.builder()
                    .id(document.getId())
                    .photoLabId(document.getPhotoLab().getId())
                    .documentType(document.getDocumentType())
                    .documentUrl(document.getObjectPath())
                    .fileName(document.getFileName())
                    .build();
        }
    }
}
