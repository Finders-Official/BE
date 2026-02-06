package com.finders.api.infra.discord;

import com.finders.api.infra.discord.dto.DiscordMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@EnableConfigurationProperties(DiscordProperties.class)
public class DiscordWebhookService {

    private static final int EMBED_COLOR_ERROR = 0xE74C3C;
    private static final int STACK_TRACE_MAX_LINES = 5;
    private static final int FIELD_MAX_LENGTH = 900;
    private static final long DEDUPE_WINDOW_MS = 60_000;
    private static final int MAX_DEDUPE_ENTRIES = 100;

    private final WebClient webClient;
    private final DiscordProperties properties;

    private final ConcurrentHashMap<String, Long> recentErrors = new ConcurrentHashMap<>();

    public DiscordWebhookService(@Qualifier("webClient") WebClient webClient, DiscordProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public void sendErrorNotification(Exception exception, String httpMethod, String requestUri) {
        if (!properties.isEnabled()) {
            return;
        }

        if (properties.webhookUrl() == null || properties.webhookUrl().isBlank()) {
            log.warn("[DiscordWebhook] Webhook URL is not configured");
            return;
        }

        String errorKey = exception.getClass().getName() + ":" + requestUri;
        if (isDuplicate(errorKey)) {
            log.debug("[DiscordWebhook] Duplicate error skipped: {}", errorKey);
            return;
        }

        DiscordMessage message = buildMessage(exception, httpMethod, requestUri);

        webClient.post()
                .uri(properties.webhookUrl())
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        response -> log.debug("[DiscordWebhook] Notification sent successfully"),
                        error -> log.error("[DiscordWebhook] Failed to send webhook", error)
                );
    }

    private boolean isDuplicate(String errorKey) {
        long now = System.currentTimeMillis();

        if (recentErrors.size() > MAX_DEDUPE_ENTRIES) {
            recentErrors.entrySet().removeIf(entry -> now - entry.getValue() > DEDUPE_WINDOW_MS);
        }

        Long lastSeen = recentErrors.putIfAbsent(errorKey, now);
        if (lastSeen == null) {
            return false;
        }

        if (now - lastSeen > DEDUPE_WINDOW_MS) {
            recentErrors.put(errorKey, now);
            return false;
        }

        return true;
    }

    private DiscordMessage buildMessage(Exception exception, String httpMethod, String requestUri) {
        String exceptionName = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage() != null ? exception.getMessage() : "No message";

        List<DiscordMessage.Field> fields = List.of(
                new DiscordMessage.Field("Exception", exceptionName, true),
                new DiscordMessage.Field("Method", httpMethod, true),
                new DiscordMessage.Field("URL", requestUri, false),
                new DiscordMessage.Field("Message", truncate(exceptionMessage, FIELD_MAX_LENGTH), false),
                new DiscordMessage.Field("Stack Trace", getStackTracePreview(exception), false)
        );

        DiscordMessage.Embed embed = new DiscordMessage.Embed(
                "Server Error",
                null,
                EMBED_COLOR_ERROR,
                fields,
                Instant.now()
        );

        return new DiscordMessage(embed);
    }

    private String getStackTracePreview(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String fullStack = filterSensitiveInfo(sw.toString());

        String[] lines = fullStack.split("\n");
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < Math.min(STACK_TRACE_MAX_LINES, lines.length); i++) {
            if (i > 0) preview.append("\n");
            preview.append(lines[i]);
        }

        return truncate(preview.toString(), FIELD_MAX_LENGTH);
    }

    // regex: 민감정보 패턴 매칭 (password=xxx, token=xxx 등)
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
