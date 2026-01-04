package com.finders.api.domain.store.entity;

import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "photo_lab_image",
        indexes = {
                @Index(name = "idx_lab_image", columnList = "photo_lab_id, is_main")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoLabImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    @Builder
    private PhotoLabImage(
            PhotoLab photoLab,
            String imageUrl,
            Integer displayOrder,
            Boolean isMain
    ) {
        this.photoLab = photoLab;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isMain = isMain != null && isMain;
    }
}
