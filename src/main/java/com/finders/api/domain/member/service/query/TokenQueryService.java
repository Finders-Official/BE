package com.finders.api.domain.member.service.query;

public interface TokenQueryService {

    int getBalance(Long memberId);

    boolean hasEnoughTokens(Long memberId, int amount);
}
