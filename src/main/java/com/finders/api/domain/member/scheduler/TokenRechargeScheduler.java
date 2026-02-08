package com.finders.api.domain.member.scheduler;

import com.finders.api.domain.member.service.command.TokenCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRechargeScheduler {

    private final TokenCommandService tokenCommandService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleDailyRecharge() {
        log.info("[TokenRechargeScheduler.scheduleDailyRecharge] 자정 토큰 자동 충전 스케줄러 가동");
        tokenCommandService.rechargeDailyTokens();
    }
}
