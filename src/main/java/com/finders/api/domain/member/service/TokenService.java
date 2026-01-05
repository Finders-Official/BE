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

    @Transactional
    public void useTokens(Long memberId, int amount, String relatedType, Long relatedId, String description) {
        MemberUser member = getMemberUser(memberId);

        if (!member.hasEnoughTokens(amount)) {
            throw new CustomException(ErrorCode.INSUFFICIENT_TOKEN);
        }

        // 토큰 차감
        int balanceAfter = member.deductTokens(amount);

        // 이력 저장
        TokenRelatedType tokenRelatedType = parseRelatedType(relatedType);
        TokenHistory history = TokenHistory.createUseHistory(
                member, amount, balanceAfter, tokenRelatedType, relatedId, description
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenService] Used tokens: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    @Transactional
    public void refundTokens(Long memberId, int amount, String relatedType, Long relatedId, String description) {
        MemberUser member = getMemberUser(memberId);

        // 토큰 추가
        int balanceAfter = member.addTokens(amount);

        // 이력 저장
        TokenRelatedType tokenRelatedType = parseRelatedType(relatedType);
        TokenHistory history = TokenHistory.createRefundHistory(
                member, amount, balanceAfter, tokenRelatedType, relatedId, description
        );
        tokenHistoryRepository.save(history);

        log.info("[TokenService] Refunded tokens: memberId={}, amount={}, balanceAfter={}, relatedType={}, relatedId={}",
                memberId, amount, balanceAfter, relatedType, relatedId);
    }

    private MemberUser getMemberUser(Long memberId) {
        return memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private TokenRelatedType parseRelatedType(String relatedType) {
        if (relatedType == null) {
            return null;
        }
        try {
            return TokenRelatedType.valueOf(relatedType);
        } catch (IllegalArgumentException e) {
            log.warn("[TokenService] Unknown relatedType: {}", relatedType);
            return null;
        }
    }
}
