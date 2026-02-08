package com.finders.api.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정
 * <p>
 * 일반 API 호출용과 장시간 작업용(이미지 다운로드 등)을 분리하여 제공합니다.
 */
@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 10000; // 10초
    private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024; // 10MB (이미지 다운로드 대응)

    /**
     * 기본 WebClient (일반 REST API 호출용)
     * <p>
     * 타임아웃: 연결 10초, 읽기/쓰기 30초, 응답 30초
     */
    @Bean
    @Primary
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                );

        return builder.clone()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * 장시간 작업용 WebClient (이미지 다운로드 등)
     * <p>
     * 타임아웃: 연결 10초, 읽기 120초, 쓰기 10초, 응답 120초
     * <p>
     * 사용 예: Replicate AI 복원 결과 이미지 다운로드
     */
    @Bean("longTimeoutWebClient")
    public WebClient longTimeoutWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(Duration.ofSeconds(120))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(120, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        return builder.clone()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
