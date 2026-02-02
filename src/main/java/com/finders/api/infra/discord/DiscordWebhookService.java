package com.finders.api.infra.discord;

/**
 * Discord Webhook 알림 서비스 인터페이스
 */
public interface DiscordWebhookService {

    /**
     * 에러 발생 시 Discord로 알림을 전송합니다.
     * 비동기로 실행되며, 알림 실패 시 예외를 던지지 않습니다.
     *
     * @param exception  발생한 예외
     * @param httpMethod HTTP 메서드 (GET, POST 등)
     * @param requestUri 요청 URI
     */
    void sendErrorNotification(Exception exception, String httpMethod, String requestUri);
}
