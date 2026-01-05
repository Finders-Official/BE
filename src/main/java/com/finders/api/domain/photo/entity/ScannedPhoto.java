package com.finders.api.domain.photo.entity;

import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "scanned_photo",
        indexes = {
                @Index(name = "idx_scanned_order", columnList = "order_id")
        }
)
public class ScannedPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DevelopmentOrder order;

    @Column(name = "image_key", length = 500, nullable = false)
    private String imageKey;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

}
