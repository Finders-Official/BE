package com.finders.api.domain.store.entity;

import com.finders.api.domain.store.enums.NoticeType;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "photo_lab_notice",
        indexes = {
                @Index(name = "idx_lab_notice", columnList = "photo_lab_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhotoLabNotice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 20)
    private NoticeType noticeType = NoticeType.GENERAL;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
