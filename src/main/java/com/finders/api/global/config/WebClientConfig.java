package com.finders.api.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 범용 WebClient 설정
 * <p>
 * 외부 API 호출 시 타임아웃 설정을 적용합니다.
 */
@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 10000; // 10초
    private static final int READ_TIMEOUT_SECONDS = 120;     // 2분 (Replicate 이미지 다운로드)
    private static final int WRITE_TIMEOUT_SECONDS = 10;     // 10초
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(120); // 2분

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(RESPONSE_TIMEOUT)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                );

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
