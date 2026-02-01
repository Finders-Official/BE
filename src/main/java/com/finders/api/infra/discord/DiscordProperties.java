package com.finders.api.infra.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Discord Webhook 설정
 */
@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(
        String webhookUrl,
        Boolean enabled
) {
    public DiscordProperties {
        if (enabled == null) {
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
