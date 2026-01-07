package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.TokenHistoryType;
import com.finders.api.domain.member.enums.TokenRelatedType;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token_history", indexes = {
        @Index(name = "idx_token_history_member", columnList = "member_id, created_at DESC"),
        @Index(name = "idx_token_history_type", columnList = "type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenHistoryType type;

    // 변동량: +3, -1 등 (사용은 음수로 저장)
    @Column(nullable = false)
    private int amount;

    // 변동 후 잔액 (항상 0 이상)
    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_type", length = 50)
    private TokenRelatedType relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(length = 200)
    private String description;

    @Builder
    private TokenHistory(
            Member member,
            TokenHistoryType type,
            int amount,
            int balanceAfter,
            TokenRelatedType relatedType,
            Long relatedId,
            String description
    ) {
        this.member = member;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
        this.description = description;
    }

    // === 팩토리 메서드 ===

    /**
     * 토큰 사용 이력 생성
     */
    public static TokenHistory createUseHistory(Member member, int amount, int balanceAfter,
                                                TokenRelatedType relatedType, Long relatedId, String description) {
        return TokenHistory.builder()
                .member(member)
                .type(TokenHistoryType.USE)
                .amount(-amount) // 사용은 음수로 저장
                .balanceAfter(balanceAfter)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .description(description)
                .build();
    }

    /**
     * 토큰 환불 이력 생성
     */
    public static TokenHistory createRefundHistory(Member member, int amount, int balanceAfter,
                                                   TokenRelatedType relatedType, Long relatedId, String description) {
        return TokenHistory.builder()
                .member(member)
                .type(TokenHistoryType.REFUND)
                .amount(amount) // 환불은 양수로 저장
                .balanceAfter(balanceAfter)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .description(description)
                .build();
    }
}
