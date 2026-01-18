package com.finders.api.infra.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PortOneProperties.class)
public class PortOneConfig {
    // PortOneProperties를 활성화하고 PortOnePaymentService에서 직접 WebClient 사용
}
