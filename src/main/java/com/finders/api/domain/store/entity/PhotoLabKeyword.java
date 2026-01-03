package com.finders.api.domain.store.entity;

import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "photo_lab_keyword",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lab_keyword", columnNames = {"photo_lab_id", "keyword"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoLabKeyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Column(nullable = false, length = 50)
    private String keyword;

    @Builder
    private PhotoLabKeyword(PhotoLab photoLab, String keyword) {
        this.photoLab = photoLab;
        this.keyword = keyword;
    }
}
