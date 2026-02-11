package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.CreditHistoryType;
import com.finders.api.domain.member.enums.CreditRelatedType;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credit_history", indexes = {
        @Index(name = "idx_credit_history_member", columnList = "member_id, created_at DESC"),
        @Index(name = "idx_credit_history_type", columnList = "type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditHistoryType type;

    // 변동량: +3, -1 등 (사용은 음수로 저장)
    @Column(nullable = false)
    private int amount;

    // 변동 후 잔액 (항상 0 이상)
    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_type", length = 50)
    private CreditRelatedType relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(length = 200)
    private String description;

    @Builder
    private CreditHistory(
            MemberUser user,
            CreditHistoryType type,
            int amount,
            int balanceAfter,
            CreditRelatedType relatedType,
            Long relatedId,
            String description
    ) {
        this.user = user;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
        this.description = description;
    }

    // === 팩토리 메서드 ===

    /**
     * 크레딧 사용 이력 생성
     */
    public static CreditHistory createUseHistory(MemberUser user, int amount, int balanceAfter,
                                                 CreditRelatedType relatedType, Long relatedId, String description) {
        return CreditHistory.builder()
                .user(user)
                .type(CreditHistoryType.USE)
                .amount(-amount) // 사용은 음수로 저장
                .balanceAfter(balanceAfter)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .description(description)
                .build();
    }

    /**
     * 크레딧 환불 이력 생성
     */
    public static CreditHistory createRefundHistory(MemberUser user, int amount, int balanceAfter,
                                                    CreditRelatedType relatedType, Long relatedId, String description) {
        return CreditHistory.builder()
                .user(user)
                .type(CreditHistoryType.REFUND)
                .amount(amount) // 환불은 양수로 저장
                .balanceAfter(balanceAfter)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .description(description)
                .build();
    }

    /**
     * 크레딧 구매 이력 생성
     */
    public static CreditHistory createPurchaseHistory(MemberUser user, int amount, int balanceAfter,
                                                      CreditRelatedType relatedType, Long relatedId, String description) {
        return CreditHistory.builder()
                .user(user)
                .type(CreditHistoryType.PURCHASE)
                .amount(amount) // 구매는 양수로 저장
                .balanceAfter(balanceAfter)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .description(description)
                .build();
    }
}
