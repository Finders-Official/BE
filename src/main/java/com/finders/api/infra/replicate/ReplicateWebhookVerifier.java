package com.finders.api.infra.replicate;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Replicate Webhook 서명 검증
 * <p>
 * Replicate Webhook Security 검증 로직 구현
 * - HMAC-SHA256 서명 검증
 * - Replay Attack 방지 (타임스탬프 검증)
 * - Timing Attack 방지 (constant-time 비교)
 *
 * @see <a href="https://replicate.com/docs/topics/webhooks/verify-webhook">Replicate Webhook Verification</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplicateWebhookVerifier {

    private static final String WEBHOOK_SECRET_PREFIX = "whsec_";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_VERSION_PREFIX = "v1,";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300; // 5분

    private final ReplicateProperties properties;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /**
     * Webhook 요청 검증
     *
     * @param webhookId        웹훅 ID (헤더: webhook-id)
     * @param webhookTimestamp 웹훅 타임스탬프 초 단위 (헤더: webhook-timestamp)
     * @param webhookSignature 웹훅 서명 (헤더: webhook-signature)
     * @param requestBody      요청 본문 (원본 그대로, 수정 금지)
     * @throws CustomException 서명이 유효하지 않거나 타임스탬프가 만료된 경우
     */
    public void verify(String webhookId, String webhookTimestamp, String webhookSignature, String requestBody) {
        // 1. 필수 헤더 검증
        if (webhookId == null || webhookId.isBlank()) {
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "webhook-id 헤더가 없습니다.");
        }
        if (webhookTimestamp == null || webhookTimestamp.isBlank()) {
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "webhook-timestamp 헤더가 없습니다.");
        }
        if (webhookSignature == null || webhookSignature.isBlank()) {
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "webhook-signature 헤더가 없습니다.");
        }

        // 2. Webhook Secret 검증 (환경별 처리)
        if (properties.webhookSecret() == null || properties.webhookSecret().isBlank()) {
            if ("local".equals(activeProfile)) {
                log.warn("[ReplicateWebhookVerifier.verify] Local 환경: Webhook secret 미설정, 검증 스킵");
                return;
            }
            log.error("[ReplicateWebhookVerifier.verify] Webhook secret이 설정되지 않았습니다. 환경: {}", activeProfile);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Webhook secret이 설정되지 않았습니다.");
        }

        // 3. Timestamp 검증 (Replay Attack 방지)
        verifyTimestamp(webhookTimestamp);

        // 4. 서명 검증
        verifySignature(webhookId, webhookTimestamp, webhookSignature, requestBody);

        log.debug("[ReplicateWebhookVerifier.verify] Webhook 검증 성공: id={}", webhookId);
    }

    /**
     * 타임스탬프 검증 (Replay Attack 방지)
     */
    private void verifyTimestamp(String webhookTimestamp) {
        try {
            long timestamp = Long.parseLong(webhookTimestamp);
            long now = System.currentTimeMillis() / 1000; // 초 단위

            long diff = Math.abs(now - timestamp);
            if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
                throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED,
                        String.format("Webhook 타임스탬프가 만료되었습니다. (diff: %d초)", diff));
            }
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "잘못된 webhook-timestamp 형식입니다.");
        }
    }

    /**
     * 서명 검증 (HMAC-SHA256)
     */
    private void verifySignature(String webhookId, String webhookTimestamp, String webhookSignature, String requestBody) {
        // 1. Signing Secret 추출 (whsec_ 제거)
        String signingSecret = extractSigningSecret(properties.webhookSecret());

        // 2. Signed Content 생성: {id}.{timestamp}.{body}
        String signedContent = String.format("%s.%s.%s", webhookId, webhookTimestamp, requestBody);

        // 3. 예상 서명 계산
        String expectedSignature = calculateHmacSha256(signedContent, signingSecret);

        // 4. 실제 서명 추출 (v1, 접두사 제거)
        String actualSignature = extractSignature(webhookSignature);

        // 5. Constant-time 비교 (Timing Attack 방지)
        if (!constantTimeEquals(expectedSignature, actualSignature)) {
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "Webhook 서명이 일치하지 않습니다.");
        }
    }

    /**
     * Webhook Secret에서 서명 키 추출 (whsec_ 제거)
     */
    private String extractSigningSecret(String webhookSecret) {
        if (webhookSecret.startsWith(WEBHOOK_SECRET_PREFIX)) {
            return webhookSecret.substring(WEBHOOK_SECRET_PREFIX.length());
        }
        return webhookSecret;
    }

    /**
     * Webhook Signature 헤더에서 실제 서명 추출
     * 형식: "v1,base64_signature" 또는 "v1,sig1 v1,sig2"
     */
    private String extractSignature(String webhookSignature) {
        // 공백으로 구분된 서명 중 첫 번째 사용
        String firstSignature = webhookSignature.split(" ")[0];

        // v1, 접두사 제거
        if (firstSignature.startsWith(SIGNATURE_VERSION_PREFIX)) {
            return firstSignature.substring(SIGNATURE_VERSION_PREFIX.length());
        }
        return firstSignature;
    }

    /**
     * HMAC-SHA256 서명 계산
     */
    private String calculateHmacSha256(String data, String secret) {
        try {
            // Base64 디코딩된 키 사용
            byte[] secretBytes = Base64.getDecoder().decode(secret);

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Base64 인코딩 반환
            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (IllegalArgumentException e) {
            log.error("[ReplicateWebhookVerifier.calculateHmacSha256] Base64 디코딩 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "잘못된 webhook secret 형식입니다.");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[ReplicateWebhookVerifier.calculateHmacSha256] HMAC 계산 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED, "서명 계산 중 오류가 발생했습니다.");
        }
    }

    /**
     * Constant-time 문자열 비교 (Timing Attack 방지)
     * <p>
     * MessageDigest.isEqual()을 사용하여 타이밍 공격 방지
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
