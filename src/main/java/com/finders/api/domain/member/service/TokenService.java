package com.finders.api.domain.member.service;

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

/**
 * 토큰 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final MemberUserRepository memberUserRepository;
    private final TokenHistoryRepository tokenHistoryRepository;

    public int getBalance(Long memberId) {
        MemberUser member = getMemberUser(memberId);
        return member.getTokenBalance();
    }

    public boolean hasEnoughTokens(Long memberId, int amount) {
        MemberUser member = getMemberUser(memberId);
        return member.hasEnoughTokens(amount);
    }

    /**
     * 토큰 사용 (동시성 제어 적용)
     * <p>
     * PESSIMISTIC_WRITE 락을 사용하여 토큰 잔액 체크와 차감 사이의 race condition을 방지합니다.
     */
    @Transactional
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

        log.info("[TokenService.useTokens] Used tokens: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    /**
     * 토큰 환불 (동시성 제어 적용)
     */
    @Transactional
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

        log.info("[TokenService.refundTokens] Refunded tokens: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    /**
     * 토큰 구매 (결제 완료 후 호출)
     */
    @Transactional
    public void purchaseTokens(MemberUser member, int amount, Long paymentId) {
        // 토큰 추가
        int balanceAfter = member.addTokens(amount);

        // 이력 저장
        TokenHistory history = TokenHistory.createPurchaseHistory(
                member, amount, balanceAfter, TokenRelatedType.PAYMENT, paymentId,
                "토큰 " + amount + "개 구매"
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenService.purchaseTokens] Purchased tokens: memberId={}, amount={}, balanceAfter={}, paymentId={}",
                member.getId(), amount, balanceAfter, paymentId);
    }

    /**
     * 토큰 회수 (결제 취소 시 호출)
     * - 결제 취소로 인한 토큰 차감은 '사용'으로 기록
     */
    @Transactional
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

        log.info("[TokenService.revokeTokens] Revoked tokens for cancellation: memberId={}, amount={}, balanceAfter={}, paymentId={}",
                member.getId(), revokeAmount, balanceAfter, paymentId);
    }

    private MemberUser getMemberUser(Long memberId) {
        return memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 배타적 락으로 회원 조회 (동시성 제어)
     * <p>
     * PESSIMISTIC_WRITE 락을 사용하여 토큰 잔액 변경 작업의 원자성을 보장합니다.
     */
    private MemberUser getMemberUserWithLock(Long memberId) {
        return memberUserRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
