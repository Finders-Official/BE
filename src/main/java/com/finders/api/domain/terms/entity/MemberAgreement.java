package com.finders.api.domain.terms.entity;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_agreement",
        indexes = {
                @Index(name = "idx_agreement_member", columnList = "member_id"),
                @Index(name = "idx_agreement_terms", columnList = "terms_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(nullable = false)
    private boolean isAgreed;

    @Column(nullable = false)
    private LocalDateTime agreedAt;

    @Builder
    private MemberAgreement(Member member, Terms terms, boolean isAgreed, LocalDateTime agreedAt) {
        this.member = member;
        this.terms = terms;
        this.isAgreed = isAgreed;
        this.agreedAt = agreedAt;
    }
}
