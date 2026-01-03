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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 20)
    private NoticeType noticeType = NoticeType.GENERAL;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    private PhotoLabNotice(
            PhotoLab photoLab,
            String title,
            String content,
            NoticeType noticeType,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive
    ) {
        this.photoLab = photoLab;
        this.title = title;
        this.content = content;
        this.noticeType = noticeType != null ? noticeType : NoticeType.GENERAL;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive == null || isActive;
    }
}
