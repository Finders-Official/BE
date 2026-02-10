package com.finders.api.domain.photo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "restoration")
public record RestorationProperties(
        String activeTier
) {
    public RestorationProperties {
        if (activeTier == null || activeTier.isBlank()) {
            activeTier = "BASIC";
        }
    }
}
