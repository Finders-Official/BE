package com.finders.api.infra.replicate;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Replicate API 설정
 */
@ConfigurationProperties(prefix = "replicate")
public record ReplicateProperties(
        String apiKey,
        String baseUrl,
        String modelVersion,
        String webhookBaseUrl,
        String webhookSecret,
        Integer timeoutSeconds,
        Integer maxRetries
) {
    public ReplicateProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.replicate.com/v1";
        }
        if (timeoutSeconds == null) {
            timeoutSeconds = 120;
        }
        if (maxRetries == null) {
            maxRetries = 60;
        }
    }

    public String getWebhookUrl() {
        return webhookBaseUrl + "/webhooks/replicate";
    }
}
