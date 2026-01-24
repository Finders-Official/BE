package com.finders.api.global.config;

import io.sendon.Sendon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "prod"})
public class SendConfig {

    @Bean
    public Sendon sendon(
            @Value("${sendon.id}") String id,
            @Value("${sendon.api-key}") String apiKey,
            @Value("${sendon.is-test}") boolean isTest
    ) {
        return Sendon.getInstance(id, apiKey, isTest);
    }
}
