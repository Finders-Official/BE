package com.finders.api.domain.terms.entity;

import com.finders.api.domain.terms.enums.TermsType;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "terms",
        indexes = {
                @Index(name = "idx_terms_active", columnList = "type, is_active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermsType type;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isRequired;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private LocalDate effectiveDate;

}
