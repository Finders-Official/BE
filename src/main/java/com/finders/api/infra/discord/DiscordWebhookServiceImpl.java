package com.finders.api.infra.discord;

import com.finders.api.infra.discord.dto.DiscordMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Discord Webhook 알림 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordWebhookServiceImpl implements DiscordWebhookService {

    private final WebClient discordWebClient;
    private final DiscordProperties properties;

    // 중복 알림 방지를 위한 최근 에러 추적 (1분 내 동일 에러 스킵)
    private final Set<String> recentErrors = ConcurrentHashMap.newKeySet();

    public void sendErrorNotification(Exception exception, HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return;
        }

        if (properties.webhookUrl() == null || properties.webhookUrl().isBlank()) {
            log.warn("[DiscordWebhookClient] Webhook URL is not configured");
            return;
        }

        // 중복 에러 체크
        String errorKey = generateErrorKey(exception, request);
        if (!recentErrors.add(errorKey)) {
            log.debug("[DiscordWebhookClient] Duplicate error skipped: {}", errorKey);
            return;
        }

        // 1분 후 중복 체크 해제
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(60000);
                recentErrors.remove(errorKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 비동기 전송 (Virtual Thread)
        Thread.ofVirtual().start(() -> {
            try {
                sendWebhookRequest(exception, request);
            } catch (Exception e) {
                log.error("[DiscordWebhookClient] Failed to send Discord notification: {}", e.getMessage());
            }
        });
    }

    private void sendWebhookRequest(Exception exception, HttpServletRequest request) {
        DiscordMessage.Embed embed = buildEmbed(exception, request);
        DiscordMessage message = new DiscordMessage(embed);

        discordWebClient.post()
                .uri(properties.webhookUrl())
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        response -> log.debug("[DiscordWebhookClient] Notification sent successfully"),
                        error -> log.error("[DiscordWebhookClient] Failed to send webhook: {}", error.getMessage())
                );
    }

    private DiscordMessage.Embed buildEmbed(Exception exception, HttpServletRequest request) {
        String exceptionName = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage() : "No message";
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        List<DiscordMessage.Field> fields = new ArrayList<>();
        fields.add(new DiscordMessage.Field("Exception", exceptionName, true));
        fields.add(new DiscordMessage.Field("Method", method, true));
        fields.add(new DiscordMessage.Field("URL", requestUri, false));
        fields.add(new DiscordMessage.Field("Message", truncate(message, 1000), false));

        // Stack trace (최상위 5줄만)
        String stackTrace = getStackTracePreview(exception);
        fields.add(new DiscordMessage.Field("Stack Trace", stackTrace, false));

        return new DiscordMessage.Embed(
                "🚨 서버 에러 발생",
                null,
                15158332, // Red color
                fields,
                Instant.now()
        );
    }

    private String generateErrorKey(Exception exception, HttpServletRequest request) {
        return exception.getClass().getName() + ":" + request.getRequestURI();
    }

    private String getStackTracePreview(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String fullStack = sw.toString();

        // 민감정보 필터링
        String filtered = filterSensitiveInfo(fullStack);

        // 첫 5줄만 추출
        String[] lines = filtered.split("\n");
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            if (i > 0) preview.append("\n");
            preview.append(lines[i]);
        }

        return truncate(preview.toString(), 1000);
    }

    private String filterSensitiveInfo(String stackTrace) {
        return stackTrace
                .replaceAll("(?i)password[=:]\\s*\\S+", "password=***")
                .replaceAll("(?i)token[=:]\\s*\\S+", "token=***")
                .replaceAll("(?i)secret[=:]\\s*\\S+", "secret=***")
                .replaceAll("(?i)api[_-]?key[=:]\\s*\\S+", "api_key=***")
                .replaceAll("(?i)authorization[=:]\\s*\\S+", "authorization=***");
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
