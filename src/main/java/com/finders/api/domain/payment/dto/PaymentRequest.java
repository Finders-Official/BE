package com.finders.api.domain.payment.dto;

import com.finders.api.domain.payment.enums.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PaymentRequest {

    public record PreRegister(
            @NotBlank(message = "결제 ID는 필수입니다")
            String paymentId,

            @NotNull(message = "주문 유형은 필수입니다")
            OrderType orderType,

            Long relatedOrderId,

            @NotBlank(message = "주문명은 필수입니다")
            String orderName,

            @NotNull(message = "결제 금액은 필수입니다")
            @Min(value = 100, message = "최소 결제 금액은 100원입니다")
            Integer amount,

            @Min(value = 1, message = "크레딧 수량은 1개 이상이어야 합니다")
            Integer creditAmount
    ) {}

    public record Complete(
            @NotBlank(message = "결제 ID는 필수입니다")
            String paymentId
    ) {}

    public record Cancel(
            @Min(value = 1, message = "취소 금액은 1원 이상이어야 합니다")
            Integer amount,  // null이면 전액 취소

            @Size(max = 200, message = "취소 사유는 200자 이내여야 합니다")
            String reason
    ) {}
}
