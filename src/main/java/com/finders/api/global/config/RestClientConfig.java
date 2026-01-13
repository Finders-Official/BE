package com.finders.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient kakaoRestClient() {
        return RestClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }
}
