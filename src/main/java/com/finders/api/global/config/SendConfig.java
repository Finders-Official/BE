package com.finders.api.global.config;

import io.sendon.Sendon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendConfig {

    @Value("${sendon.id}")
    private String id;

    @Value("${sendon.api-key}")
    private String apiKey;

    @Value("${sendon.is-test}")
    private boolean isTest;

    @Bean
    public Sendon sendon() {
        return Sendon.getInstance(id, apiKey, isTest);
    }
}
