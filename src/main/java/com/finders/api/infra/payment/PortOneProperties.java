package com.finders.api.infra.payment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {

    private String apiSecret;
    private String webhookSecret;
    private String storeId;
}
