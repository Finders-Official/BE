package com.finders.api.infra.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포트원 V2 결제 취소 API 응답 DTO
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOneCancelResponse {

    private Cancellation cancellation;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cancellation {
        private String id;
        private Integer totalAmount;
    }
}
