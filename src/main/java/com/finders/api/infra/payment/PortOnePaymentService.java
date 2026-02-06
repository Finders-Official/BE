package com.finders.api.infra.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.domain.payment.enums.PaymentMethod;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.enums.PgProvider;
import com.finders.api.infra.payment.dto.PortOneCancelResponse;
import com.finders.api.infra.payment.dto.PortOnePaymentResponse;
import com.finders.api.infra.payment.dto.PortOneWebhookPayload;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 포트원 V2 REST API 클라이언트
 */
@Slf4j
@Service
public class PortOnePaymentService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient;
    private final PortOneProperties properties;

    public PortOnePaymentService(@Qualifier("portOneWebClient") WebClient webClient, PortOneProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /**
     * 결제 정보 조회
     */
    public PortOnePaymentInfo getPayment(String paymentId) {
        try {
            PortOnePaymentResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/payments/{paymentId}")
                            .queryParam("storeId", properties.getStoreId())
                            .build(paymentId))
                    .retrieve()
                    .bodyToMono(PortOnePaymentResponse.class)
                    .block();

            return convertToPaymentInfo(response);
        } catch (WebClientResponseException e) {
            log.error("[PortOnePaymentService.getPayment] 포트원 결제 조회 실패: paymentId={}, status={}, body={}",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new PortOneException("결제 정보를 조회할 수 없습니다.", e);
        } catch (Exception e) {
            log.error("[PortOnePaymentService.getPayment] 포트원 결제 조회 실패: paymentId={}", paymentId, e);
            throw new PortOneException("결제 정보를 조회할 수 없습니다.", e);
        }
    }

    /**
     * 결제 취소
     */
    public PortOneCancelInfo cancelPayment(String paymentId, Integer amount, String reason) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            if (amount != null) {
                requestBody.put("amount", amount);
            }
            if (reason != null) {
                requestBody.put("reason", reason);
            }
            requestBody.put("storeId", properties.getStoreId());

            PortOneCancelResponse response = webClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(PortOneCancelResponse.class)
                    .block();

            PortOneCancelResponse.Cancellation cancellation = response.getCancellation();

            return new PortOneCancelInfo(
                    cancellation.getId(),
                    cancellation.getTotalAmount() != null ? cancellation.getTotalAmount() : 0
            );
        } catch (WebClientResponseException e) {
            log.error("[PortOnePaymentService.cancelPayment] 포트원 결제 취소 실패: paymentId={}, status={}, body={}",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new PortOneException("결제를 취소할 수 없습니다.", e);
        } catch (Exception e) {
            log.error("[PortOnePaymentService.cancelPayment] 포트원 결제 취소 실패: paymentId={}", paymentId, e);
            throw new PortOneException("결제를 취소할 수 없습니다.", e);
        }
    }

    /**
     * 웹훅 검증 및 파싱
     * 포트원 V2 웹훅 시그니처 검증 (HMAC-SHA256)
     */
    public PortOneWebhookInfo verifyWebhook(String body, String webhookId,
                                            String webhookTimestamp, String webhookSignature) {
        try {
            // 시그니처 검증
            if (!verifyWebhookSignature(body, webhookId, webhookTimestamp, webhookSignature)) {
                throw new PortOneException("웹훅 시그니처 검증에 실패했습니다.");
            }

            // JSON 파싱 (DTO 사용)
            PortOneWebhookPayload webhook = objectMapper.readValue(body, PortOneWebhookPayload.class);

            PortOneWebhookPayload.Data data = webhook.getData();
            if (data != null && data.getPaymentId() != null) {
                return new PortOneWebhookInfo(data.getPaymentId(), webhook.getType());
            }

            return null;
        } catch (PortOneException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PortOnePaymentService.verifyWebhook] 웹훅 검증 실패", e);
            throw new PortOneException("웹훅 검증에 실패했습니다.", e);
        }
    }

    /**
     * 웹훅 시그니처 검증
     * - 상수 시간 비교(constant-time comparison)로 타이밍 공격 방지
     * - 포트원 웹훅 시그니처는 여러 개가 콤마로 구분되어 전달될 수 있음
     */
    private boolean verifyWebhookSignature(String body, String webhookId,
                                           String webhookTimestamp, String webhookSignature) {
        try {
            String signedPayload = webhookId + "." + webhookTimestamp + "." + body;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = "v1," + Base64.getEncoder().encodeToString(hash);

            // 웹훅 시그니처는 여러 개가 공백으로 구분되어 전달될 수 있음
            // 상수 시간 비교로 타이밍 공격 방지
            for (String signature : webhookSignature.split(" ")) {
                if (java.security.MessageDigest.isEqual(
                        signature.trim().getBytes(StandardCharsets.UTF_8),
                        expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("[PortOnePaymentService.verifyWebhookSignature] 시그니처 검증 중 오류 발생", e);
            return false;
        }
    }

    private PortOnePaymentInfo convertToPaymentInfo(PortOnePaymentResponse response) {
        // 금액 추출
        Integer totalAmount = null;
        if (response.getAmount() != null) {
            totalAmount = response.getAmount().getTotal();
        }

        // 카드 정보 추출
        String cardCompany = null;
        String cardNumber = null;
        String approveNo = null;
        Integer installmentMonths = null;

        PortOnePaymentResponse.Method method = response.getMethod();
        if (method != null) {
            PortOnePaymentResponse.Card card = method.getCard();
            if (card != null) {
                cardCompany = card.getPublisher();
                cardNumber = card.getNumber();
            }
            approveNo = method.getApprovalNumber();

            PortOnePaymentResponse.Installment installment = method.getInstallment();
            if (installment != null) {
                installmentMonths = installment.getMonth();
            }
        }

        // 실패 정보 추출
        PortOnePaymentResponse.Failure failure = response.getFailure();

        return PortOnePaymentInfo.builder()
                .paymentId(response.getId())
                .transactionId(response.getTransactionId())
                .status(convertStatus(response.getStatus()))
                .amount(totalAmount)
                .method(convertMethod(method))
                .pgProvider(convertPgProvider(response.getChannel()))
                .pgTxId(response.getPgTxId())
                .receiptUrl(response.getReceiptUrl())
                .cardCompany(cardCompany)
                .cardNumber(cardNumber)
                .approveNo(approveNo)
                .installmentMonths(installmentMonths)
                .failCode(failure != null ? failure.getReason() : null)
                .failMessage(failure != null ? failure.getMessage() : null)
                .build();
    }

    private PaymentStatus convertStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "READY" -> PaymentStatus.READY;
            case "PENDING" -> PaymentStatus.PENDING;
            case "VIRTUAL_ACCOUNT_ISSUED" -> PaymentStatus.VIRTUAL_ACCOUNT_ISSUED;
            case "PAID" -> PaymentStatus.PAID;
            case "FAILED" -> PaymentStatus.FAILED;
            case "PARTIAL_CANCELLED" -> PaymentStatus.PARTIAL_CANCELLED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            default -> null;
        };
    }

    private PaymentMethod convertMethod(PortOnePaymentResponse.Method method) {
        if (method == null || method.getType() == null) return null;

        return switch (method.getType()) {
            case "PaymentMethodCard" -> PaymentMethod.CARD;
            case "PaymentMethodTransfer" -> PaymentMethod.TRANSFER;
            case "PaymentMethodVirtualAccount" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "PaymentMethodEasyPay" -> PaymentMethod.EASY_PAY;
            default -> null;  // MOBILE 등 ERD에 정의되지 않은 결제 수단은 null 처리
        };
    }

    private PgProvider convertPgProvider(PortOnePaymentResponse.Channel channel) {
        if (channel == null || channel.getPgProvider() == null) return null;

        return switch (channel.getPgProvider()) {
            case "KCP" -> PgProvider.KCP;
            case "KAKAOPAY" -> PgProvider.KAKAOPAY;
            case "NAVERPAY" -> PgProvider.NAVERPAY;
            case "TOSSPAY" -> PgProvider.TOSSPAY;
            case "TOSSPAYMENTS" -> PgProvider.TOSSPAYMENTS;
            default -> null;
        };
    }

    /**
     * 실제 결제인지 검증 (테스트 결제 필터링)
     */
    public boolean isLivePayment(String paymentId) {
        try {
            PortOnePaymentInfo info = getPayment(paymentId);
            return info != null && info.getStatus() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
