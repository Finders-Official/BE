package com.finders.api.infra.discord;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Discord Webhook 알림 서비스 인터페이스
 */
public interface DiscordWebhookService {

    /**
     * 에러 발생 시 Discord로 알림을 전송합니다.
     * 비동기로 실행되며, 알림 실패 시 예외를 던지지 않습니다.
     *
     * @param exception 발생한 예외
     * @param request HTTP 요청 정보
     */
    void sendErrorNotification(Exception exception, HttpServletRequest request);
}
