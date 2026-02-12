package com.finders.api.domain.payment.controller;

import com.finders.api.domain.payment.dto.PaymentRequest;
import com.finders.api.domain.payment.dto.PaymentResponse;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.service.command.PaymentCommandService;
import com.finders.api.domain.payment.service.query.PaymentQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    @Operation(
            summary = "결제 사전등록",
            description = "결제창 호출 전 결제 정보를 사전등록합니다. paymentId는 프론트에서 UUID로 생성하여 전달합니다."
    )
    @PostMapping("/pre-register")
    public ApiResponse<PaymentResponse.PreRegistered> preRegister(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid PaymentRequest.PreRegister request
    ) {
        PaymentResponse.PreRegistered response = paymentCommandService.preRegister(
                authUser.memberId(), request);
        return ApiResponse.success(SuccessCode.PAYMENT_PRE_REGISTERED, response);
    }

    @Operation(
            summary = "결제 완료 처리",
            description = "결제창에서 결제 완료 후 호출합니다. 포트원에서 결제 정보를 조회하여 검증하고 저장합니다."
    )
    @PostMapping("/complete")
    public ApiResponse<PaymentResponse.Detail> complete(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid PaymentRequest.Complete request
    ) {
        PaymentResponse.Detail response = paymentCommandService.complete(
                authUser.memberId(), request);
        return ApiResponse.success(SuccessCode.PAYMENT_COMPLETED, response);
    }

    @Operation(
            summary = "결제 상세 조회",
            description = "결제 상세 정보를 조회합니다."
    )
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse.Detail> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String paymentId
    ) {
        PaymentResponse.Detail response = paymentQueryService.getPayment(
                authUser.memberId(), paymentId);
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @Operation(
            summary = "내 결제 목록 조회",
            description = "내 결제 목록을 조회합니다. status 파라미터로 상태별 필터링이 가능합니다."
    )
    @GetMapping
    public ApiResponse<List<PaymentResponse.Summary>> getMyPayments(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) PaymentStatus status
    ) {
        List<PaymentResponse.Summary> response;
        if (status != null) {
            response = paymentQueryService.getMyPaymentsByStatus(authUser.memberId(), status);
        } else {
            response = paymentQueryService.getMyPayments(authUser.memberId());
        }
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @Operation(
            summary = "결제 취소",
            description = "결제를 취소합니다. 부분 취소 시 amount를 지정하고, 전액 취소 시 amount를 생략합니다."
    )
    @PostMapping("/{paymentId}/cancel")
    public ApiResponse<PaymentResponse.Cancelled> cancel(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String paymentId,
            @RequestBody(required = false) @Valid PaymentRequest.Cancel request
    ) {
        if (request == null) {
            request = new PaymentRequest.Cancel(null, null);
        }
        PaymentResponse.Cancelled response = paymentCommandService.cancel(
                authUser.memberId(), paymentId, request);
        return ApiResponse.success(SuccessCode.PAYMENT_CANCELLED, response);
    }
}
