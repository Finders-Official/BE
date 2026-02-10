package com.finders.api.global.config;

import com.finders.api.global.security.JwtProperties;
import com.finders.api.domain.photo.config.RestorationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, RestorationProperties.class})
public class PropertiesConfig {
}
