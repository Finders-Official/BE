package com.finders.api.domain.store.entity;

import com.finders.api.domain.store.enums.DocumentType;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "photo_lab_document",
        indexes = {
                @Index(name = "idx_lab_doc", columnList = "photo_lab_id, document_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoLabDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "object_path", nullable = false, length = 500)
    private String objectPath;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Builder
    private PhotoLabDocument(
            PhotoLab photoLab,
            DocumentType documentType,
            String objectPath,
            String fileName,
            LocalDateTime verifiedAt
    ) {
        this.photoLab = photoLab;
        this.documentType = documentType;
        this.objectPath = objectPath;
        this.fileName = fileName;
        this.verifiedAt = verifiedAt;
    }
}
