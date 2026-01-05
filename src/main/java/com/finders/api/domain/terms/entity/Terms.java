package com.finders.api.domain.terms.entity;

import com.finders.api.domain.terms.enums.TermsType;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "terms",
        indexes = {
                @Index(name = "idx_terms_active", columnList = "type, is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_terms_version", columnNames = {"type", "version"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TermsType type;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Builder
    private Terms(TermsType type, String version, String title, String content, boolean isRequired, boolean isActive, LocalDate effectiveDate) {
        this.type = type;
        this.version = version;
        this.title = title;
        this.content = content;
        this.isRequired = isRequired;
        this.isActive = isActive;
        this.effectiveDate = effectiveDate;
    }
}
