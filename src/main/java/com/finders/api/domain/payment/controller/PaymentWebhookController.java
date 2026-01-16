package com.finders.api.domain.payment.controller;

import com.finders.api.domain.payment.service.command.PaymentCommandService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 포트원 V2 웹훅 수신 컨트롤러
 * - 인증 없이 포트원 서버에서 호출
 * - 웹훅 시그니처로 검증
 */
@Slf4j
@Hidden  // Swagger에서 숨김
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class PaymentWebhookController {

    private final PaymentCommandService paymentCommandService;

    /**
     * 포트원 V2 웹훅 수신
     * - 결제 상태 변경 시 호출됨
     * - 가상계좌 입금 완료, 결제 취소 등
     */
    @PostMapping("/portone")
    public ResponseEntity<Void> handlePortOneWebhook(
            @RequestBody String body,
            @RequestHeader(value = "webhook-id", required = false) String webhookId,
            @RequestHeader(value = "webhook-timestamp", required = false) String webhookTimestamp,
            @RequestHeader(value = "webhook-signature", required = false) String webhookSignature
    ) {
        // 필수 헤더 검증
        if (!StringUtils.hasText(webhookId) ||
                !StringUtils.hasText(webhookTimestamp) ||
                !StringUtils.hasText(webhookSignature)) {
            log.warn("[PaymentWebhookController.handlePortOneWebhook] 웹훅 필수 헤더 누락: webhookId={}, hasTimestamp={}, hasSignature={}",
                    webhookId, StringUtils.hasText(webhookTimestamp), StringUtils.hasText(webhookSignature));
            return ResponseEntity.badRequest().build();
        }

        log.info("[PaymentWebhookController.handlePortOneWebhook] 포트원 웹훅 수신: webhookId={}", webhookId);

        try {
            paymentCommandService.handleWebhook(body, webhookId, webhookTimestamp, webhookSignature);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("[PaymentWebhookController.handlePortOneWebhook] 웹훅 처리 실패: webhookId={}", webhookId, e);
            // 웹훅 재시도를 위해 400 대신 200 반환하지 않음
            return ResponseEntity.badRequest().build();
        }
    }
}
