package com.finders.api.domain.photo.config;

import com.finders.api.domain.photo.enums.RestorationTier;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "restoration")
public record RestorationProperties(
        String activeTier
) {
    public RestorationProperties {
        if (activeTier == null || activeTier.isBlank()) {
            activeTier = RestorationTier.BASIC.name();
        }
    }
}
