package com.finders.api.domain.member.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.TokenHistory;
import com.finders.api.domain.member.enums.TokenRelatedType;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.TokenHistoryRepository;
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
public class TokenCommandServiceImpl implements TokenCommandService {

    private final MemberUserRepository memberUserRepository;
    private final TokenHistoryRepository tokenHistoryRepository;

    public void useTokens(Long memberId, int amount, TokenRelatedType relatedType, Long relatedId, String description) {
        // 배타적 락으로 회원 조회 (동시성 제어)
        MemberUser member = getMemberUserWithLock(memberId);

        if (!member.hasEnoughTokens(amount)) {
            throw new CustomException(ErrorCode.INSUFFICIENT_TOKEN);
        }

        // 토큰 차감
        int balanceAfter = member.deductTokens(amount);

        // 이력 저장
        TokenHistory history = TokenHistory.createUseHistory(
                member, amount, balanceAfter, relatedType, relatedId, description
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenCommandServiceImpl.useTokens] Used tokens: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    public void refundTokens(Long memberId, int amount, TokenRelatedType relatedType, Long relatedId, String description) {
        // 배타적 락으로 회원 조회 (동시성 제어)
        MemberUser member = getMemberUserWithLock(memberId);

        // 토큰 추가
        int balanceAfter = member.addTokens(amount);

        // 이력 저장
        TokenHistory history = TokenHistory.createRefundHistory(
                member, amount, balanceAfter, relatedType, relatedId, description
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenCommandServiceImpl.refundTokens] Refunded tokens: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    public void purchaseTokens(MemberUser member, int amount, Long paymentId) {
        // 토큰 추가
        int balanceAfter = member.addTokens(amount);

        // 이력 저장
        TokenHistory history = TokenHistory.createPurchaseHistory(
                member, amount, balanceAfter, TokenRelatedType.PAYMENT, paymentId,
                "토큰 " + amount + "개 구매"
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenCommandServiceImpl.purchaseTokens] Purchased tokens: memberId={}, amount={}, balanceAfter={}, paymentId={}",
                member.getId(), amount, balanceAfter, paymentId);
    }

    public void revokeTokens(MemberUser member, int amount, Long paymentId) {
        int currentBalance = member.getTokenBalance();

        // 회수할 토큰이 현재 잔액보다 많으면 현재 잔액만큼만 차감
        int revokeAmount = Math.min(amount, currentBalance);
        int balanceAfter = member.deductTokens(revokeAmount);

        // 이력 저장 (사용으로 기록 - 내부에서 음수로 저장됨)
        TokenHistory history = TokenHistory.createUseHistory(
                member, revokeAmount, balanceAfter, TokenRelatedType.PAYMENT, paymentId,
                "토큰 " + revokeAmount + "개 회수 (결제 취소)"
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenCommandServiceImpl.revokeTokens] Revoked tokens for cancellation: memberId={}, amount={}, balanceAfter={}, paymentId={}",
                member.getId(), revokeAmount, balanceAfter, paymentId);
    }

    public void rechargeDailyTokens() {
        log.info("[TokenCommandServiceImpl.rechargeDailyTokens] 자동 충전 프로세스 시작");
        int updatedCount = memberUserRepository.bulkRechargeTokens();
        log.info("[TokenCommandServiceImpl.rechargeDailyTokens] 자동 충전 완료 - 대상 유저 수: {}", updatedCount);
    }

    private MemberUser getMemberUserWithLock(Long memberId) {
        return memberUserRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
