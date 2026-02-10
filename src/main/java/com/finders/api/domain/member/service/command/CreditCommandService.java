package com.finders.api.domain.member.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.TokenRelatedType;

public interface TokenCommandService {

    /**
     * 토큰 사용 (동시성 제어 적용)
     * <p>
     * PESSIMISTIC_WRITE 락을 사용하여 토큰 잔액 체크와 차감 사이의 race condition을 방지합니다.
     */
    void useTokens(Long memberId, int amount, TokenRelatedType relatedType, Long relatedId, String description);

    /**
     * 토큰 환불 (동시성 제어 적용)
     */
    void refundTokens(Long memberId, int amount, TokenRelatedType relatedType, Long relatedId, String description);

    /**
     * 토큰 구매 (결제 완료 후 호출)
     */
    void purchaseTokens(MemberUser member, int amount, Long paymentId);

    /**
     * 토큰 회수 (결제 취소 시 호출)
     * - 결제 취소로 인한 토큰 차감은 '사용'으로 기록
     */
    void revokeTokens(MemberUser member, int amount, Long paymentId);

    // 매일 자정 자동 충전 (벌크 연산)
    void rechargeDailyTokens();
}
