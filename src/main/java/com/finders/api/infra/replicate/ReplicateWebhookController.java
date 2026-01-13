package com.finders.api.infra.replicate;

import com.finders.api.domain.photo.service.PhotoRestorationService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private final PhotoRestorationService restorationService;

    @PostMapping
    public ApiResponse<Void> handleWebhook(@RequestBody ReplicateResponse.Prediction payload) {
        log.info("[ReplicateWebhook] Received: id={}, status={}", payload.id(), payload.status());

        if (payload.isSucceeded()) {
            String resultUrl = payload.getFirstOutput();
            if (resultUrl != null) {
                try {
                    restorationService.completeRestoration(payload.id(), resultUrl);
                } catch (Exception e) {
                    // completeRestoration 실패 시 별도 트랜잭션으로 실패 처리
                    log.error("[ReplicateWebhook] Failed to complete restoration: id={}, error={}",
                            payload.id(), e.getMessage(), e);
                    restorationService.failRestoration(payload.id(), e.getMessage());
                }
            } else {
                log.error("[ReplicateWebhook] Succeeded but no output: id={}", payload.id());
                restorationService.failRestoration(payload.id(), "복원 결과 이미지가 없습니다.");
            }
        } else if (payload.isFailed()) {
            restorationService.failRestoration(payload.id(), payload.error());
        } else {
            log.debug("[ReplicateWebhook] Ignored status: id={}, status={}", payload.id(), payload.status());
        }

        return ApiResponse.success(SuccessCode.OK);
    }
}
