package com.finders.api.domain.member.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.CreditHistory;
import com.finders.api.domain.member.enums.CreditRelatedType;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.CreditHistoryRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreditCommandServiceImpl implements CreditCommandService {

    private final MemberUserRepository memberUserRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    public void useCredits(Long memberId, int amount, CreditRelatedType relatedType, Long relatedId, String description) {
        // 배타적 락으로 회원 조회 (동시성 제어)
        MemberUser member = getMemberUserWithLock(memberId);

        if (!member.hasEnoughCredits(amount)) {
            throw new CustomException(ErrorCode.INSUFFICIENT_CREDIT);
        }

        // 크레딧 차감
        int balanceAfter = member.deductCredits(amount);

        // 이력 저장
        CreditHistory history = CreditHistory.createUseHistory(
                member, amount, balanceAfter, relatedType, relatedId, description
        );
        creditHistoryRepository.save(history);

        log.info("[CreditCommandServiceImpl.useCredits] Used credits: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    public void refundCredits(Long memberId, int amount, CreditRelatedType relatedType, Long relatedId, String description) {
        // 배타적 락으로 회원 조회 (동시성 제어)
        MemberUser member = getMemberUserWithLock(memberId);

        // 크레딧 추가
        int balanceAfter = member.addCredits(amount);

        // 이력 저장
        CreditHistory history = CreditHistory.createRefundHistory(
                member, amount, balanceAfter, relatedType, relatedId, description
        );
        creditHistoryRepository.save(history);

        log.info("[CreditCommandServiceImpl.refundCredits] Refunded credits: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    public void purchaseCredits(MemberUser member, int amount, Long paymentId) {
        // 크레딧 추가
        int balanceAfter = member.addCredits(amount);

        // 이력 저장
        CreditHistory history = CreditHistory.createPurchaseHistory(
                member, amount, balanceAfter, CreditRelatedType.PAYMENT, paymentId,
                "크레딧 " + amount + "개 구매"
        );
        creditHistoryRepository.save(history);

        log.info("[CreditCommandServiceImpl.purchaseCredits] Purchased credits: memberId={}, amount={}, balanceAfter={}, paymentId={}",
                member.getId(), amount, balanceAfter, paymentId);
    }

    public void revokeCredits(MemberUser member, int amount, Long paymentId) {
        int currentBalance = member.getCreditBalance();

        // 회수할 크레딧이 현재 잔액보다 많으면 현재 잔액만큼만 차감
        int revokeAmount = Math.min(amount, currentBalance);
        int balanceAfter = member.deductCredits(revokeAmount);

        // 이력 저장 (사용으로 기록 - 내부에서 음수로 저장됨)
        CreditHistory history = CreditHistory.createUseHistory(
                member, revokeAmount, balanceAfter, CreditRelatedType.PAYMENT, paymentId,
                "크레딧 " + revokeAmount + "개 회수 (결제 취소)"
        );
        creditHistoryRepository.save(history);

        log.info("[CreditCommandServiceImpl.revokeCredits] Revoked credits for cancellation: memberId={}, amount={}, balanceAfter={}, paymentId={}",
                member.getId(), revokeAmount, balanceAfter, paymentId);
    }

    public void rechargeDailyCredits() {
        log.info("[CreditCommandServiceImpl.rechargeDailyCredits] 자동 충전 프로세스 시작");
        int updatedCount = memberUserRepository.bulkRechargeCredits();
        log.info("[CreditCommandServiceImpl.rechargeDailyCredits] 자동 충전 완료 - 대상 유저 수: {}", updatedCount);
    }

    private MemberUser getMemberUserWithLock(Long memberId) {
        return memberUserRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
