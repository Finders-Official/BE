package com.finders.api.infra.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortOneWebhookInfo {
    private final String paymentId;
    private final String webhookType;
}
