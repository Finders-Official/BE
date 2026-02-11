package com.finders.api.domain.member.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.CreditRelatedType;

public interface CreditCommandService {

    /**
     * 크레딧 사용 (동시성 제어 적용)
     * <p>
     * PESSIMISTIC_WRITE 락을 사용하여 크레딧 잔액 체크와 차감 사이의 race condition을 방지합니다.
     */
    void useCredits(Long memberId, int amount, CreditRelatedType relatedType, Long relatedId, String description);

    /**
     * 크레딧 환불 (동시성 제어 적용)
     */
    void refundCredits(Long memberId, int amount, CreditRelatedType relatedType, Long relatedId, String description);

    /**
     * 크레딧 구매 (결제 완료 후 호출)
     */
    void purchaseCredits(MemberUser member, int amount, Long paymentId);

    /**
     * 크레딧 회수 (결제 취소 시 호출)
     * - 결제 취소로 인한 크레딧 차감은 '사용'으로 기록
     */
    void revokeCredits(MemberUser member, int amount, Long paymentId);

    // 매일 자정 자동 충전 (벌크 연산)
    void rechargeDailyCredits();
}
