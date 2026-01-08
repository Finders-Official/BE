package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "social_account",
        indexes = {
                @Index(name = "idx_social_member", columnList = "member_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_provider",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Builder
    private SocialAccount(MemberUser user, SocialProvider provider, String providerId, String socialEmail) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }
}
