package com.finders.api.infra.discord;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Discord Webhook 설정
 */
@Configuration
@EnableConfigurationProperties(DiscordProperties.class)
public class DiscordConfig {
}
