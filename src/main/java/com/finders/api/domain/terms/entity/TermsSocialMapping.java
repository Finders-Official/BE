package com.finders.api.domain.terms.entity;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms_social_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsSocialMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Column(nullable = false)
    private String socialTag; // 소셜 측에서 사용하는 태그 (예: "service_privacy")

    @Builder
    private TermsSocialMapping(Terms terms, SocialProvider provider, String socialTag) {
        this.terms = terms;
        this.provider = provider;
        this.socialTag = socialTag;
    }
}