package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenQueryServiceImpl implements TokenQueryService {

    private final MemberUserRepository memberUserRepository;

    public int getBalance(Long memberId) {
        MemberUser member = getMemberUser(memberId);
        return member.getTokenBalance();
    }

    public boolean hasEnoughTokens(Long memberId, int amount) {
        MemberUser member = getMemberUser(memberId);
        return member.hasEnoughTokens(amount);
    }

    private MemberUser getMemberUser(Long memberId) {
        return memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
