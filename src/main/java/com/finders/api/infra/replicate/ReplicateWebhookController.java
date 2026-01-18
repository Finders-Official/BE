package com.finders.api.infra.replicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.domain.photo.service.command.PhotoRestorationCommandService;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Replicate Webhook 콜백 컨트롤러
 * <p>
 * Replicate API가 Prediction 완료 시 호출하는 엔드포인트
 */
@Slf4j
@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks/replicate")
public class ReplicateWebhookController {

    private final PhotoRestorationCommandService commandService;
    private final ReplicateWebhookVerifier webhookVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ApiResponse<Void> handleWebhook(
            @RequestHeader(value = "webhook-id", required = false) String webhookId,
            @RequestHeader(value = "webhook-timestamp", required = false) String webhookTimestamp,
            @RequestHeader(value = "webhook-signature", required = false) String webhookSignature,
            @RequestBody String rawBody
    ) {
        // 1. Webhook 서명 검증
        webhookVerifier.verify(webhookId, webhookTimestamp, webhookSignature, rawBody);

        // 2. JSON 파싱
        ReplicateResponse.Prediction payload;
        try {
            payload = objectMapper.readValue(rawBody, ReplicateResponse.Prediction.class);
        } catch (IOException e) {
            log.error("[ReplicateWebhookController.handleWebhook] JSON 파싱 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 Webhook 페이로드 형식입니다.");
        }
        log.info("[ReplicateWebhookController.handleWebhook] Received: id={}, status={}", payload.id(), payload.status());

        if (payload.isSucceeded()) {
            String resultUrl = payload.getFirstOutput();
            if (resultUrl != null) {
                try {
                    commandService.completeRestoration(payload.id(), resultUrl);
                } catch (Exception e) {
                    // completeRestoration 실패 시 별도 트랜잭션으로 실패 처리
                    log.error("[ReplicateWebhookController.handleWebhook] Failed to complete restoration: id={}, error={}",
                            payload.id(), e.getMessage(), e);
                    commandService.failRestoration(payload.id(), e.getMessage());
                }
            } else {
                log.error("[ReplicateWebhookController.handleWebhook] Succeeded but no output: id={}", payload.id());
                commandService.failRestoration(payload.id(), "복원 결과 이미지가 없습니다.");
            }
        } else if (payload.isFailed()) {
            commandService.failRestoration(payload.id(), payload.error());
        } else {
            log.debug("[ReplicateWebhookController.handleWebhook] Ignored status: id={}, status={}", payload.id(), payload.status());
        }

        return ApiResponse.success(SuccessCode.OK);
    }
}
