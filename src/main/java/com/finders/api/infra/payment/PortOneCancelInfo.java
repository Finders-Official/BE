package com.finders.api.infra.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortOneCancelInfo {
    private final String cancellationId;
    private final Integer cancelledAmount;
}
