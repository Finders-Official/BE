package com.finders.api.domain.member.service.query;

public interface CreditQueryService {

    int getBalance(Long memberId);

    boolean hasEnoughCredits(Long memberId, int amount);
}
