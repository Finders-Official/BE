package com.finders.api.infra.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.domain.payment.enums.PaymentMethod;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.enums.PgProvider;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 포트원 V2 REST API 클라이언트
 */
@Slf4j
@Service
public class PortOnePaymentService {

    private static final String BASE_URL = "https://api.portone.io";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_SEC = 10;
    private static final int WRITE_TIMEOUT_SEC = 10;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient;
    private final PortOneProperties properties;

    public PortOnePaymentService(PortOneProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(Duration.ofSeconds(READ_TIMEOUT_SEC))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_SEC, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + properties.getApiSecret())
                .build();
    }

    /**
     * 결제 정보 조회
     */
    public PortOnePaymentInfo getPayment(String paymentId) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/payments/{paymentId}")
                            .queryParam("storeId", properties.getStoreId())
                            .build(paymentId))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return convertToPaymentInfo(response);
        } catch (WebClientResponseException e) {
            log.error("포트원 결제 조회 실패: paymentId={}, status={}, body={}",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new PortOneException("결제 정보를 조회할 수 없습니다.", e);
        } catch (Exception e) {
            log.error("포트원 결제 조회 실패: paymentId={}", paymentId, e);
            throw new PortOneException("결제 정보를 조회할 수 없습니다.", e);
        }
    }

    /**
     * 결제 취소
     */
    public PortOneCancelInfo cancelPayment(String paymentId, Integer amount, String reason) {
        try {
            Map<String, Object> requestBody = new java.util.HashMap<>();
            if (amount != null) {
                requestBody.put("amount", amount);
            }
            if (reason != null) {
                requestBody.put("reason", reason);
            }
            requestBody.put("storeId", properties.getStoreId());

            Map<String, Object> response = webClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> cancellation = (Map<String, Object>) response.get("cancellation");
            String cancelId = (String) cancellation.get("id");
            Number totalAmount = (Number) cancellation.get("totalAmount");

            return new PortOneCancelInfo(
                    cancelId,
                    totalAmount != null ? totalAmount.intValue() : 0
            );
        } catch (WebClientResponseException e) {
            log.error("포트원 결제 취소 실패: paymentId={}, status={}, body={}",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new PortOneException("결제를 취소할 수 없습니다.", e);
        } catch (Exception e) {
            log.error("포트원 결제 취소 실패: paymentId={}", paymentId, e);
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

            // JSON 파싱
            Map<String, Object> webhook = objectMapper.readValue(body, Map.class);

            String type = (String) webhook.get("type");
            Map<String, Object> data = (Map<String, Object>) webhook.get("data");

            if (data != null && data.containsKey("paymentId")) {
                return new PortOneWebhookInfo((String) data.get("paymentId"), type);
            }

            return null;
        } catch (PortOneException e) {
            throw e;
        } catch (Exception e) {
            log.error("웹훅 검증 실패", e);
            throw new PortOneException("웹훅 검증에 실패했습니다.", e);
        }
    }

    /**
     * 웹훅 시그니처 검증
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

            // 웹훅 시그니처는 "v1,{base64_signature}" 형식
            return webhookSignature.contains(expectedSignature);
        } catch (Exception e) {
            log.error("시그니처 검증 중 오류 발생", e);
            return false;
        }
    }

    private PortOnePaymentInfo convertToPaymentInfo(Map<String, Object> payment) {
        String status = (String) payment.get("status");
        String paymentId = (String) payment.get("id");
        String transactionId = (String) payment.get("transactionId");
        String pgTxId = (String) payment.get("pgTxId");
        String receiptUrl = (String) payment.get("receiptUrl");

        Map<String, Object> amount = (Map<String, Object>) payment.get("amount");
        Integer totalAmount = null;
        if (amount != null && amount.get("total") != null) {
            totalAmount = ((Number) amount.get("total")).intValue();
        }

        Map<String, Object> channel = (Map<String, Object>) payment.get("channel");
        Map<String, Object> method = (Map<String, Object>) payment.get("method");

        Map<String, Object> failure = (Map<String, Object>) payment.get("failure");

        // 카드 정보 추출
        String cardCompany = null;
        String cardNumber = null;
        String approveNo = null;
        Integer installmentMonths = null;

        if (method != null) {
            // 카드 정보 (PaymentMethodCard 구조)
            Map<String, Object> card = (Map<String, Object>) method.get("card");
            if (card != null) {
                cardCompany = (String) card.get("publisher");  // 발행사 코드
                cardNumber = (String) card.get("number");      // 마스킹된 카드 번호
            }
            approveNo = (String) method.get("approvalNumber");

            // 할부 정보 (PaymentInstallment 구조)
            Map<String, Object> installment = (Map<String, Object>) method.get("installment");
            if (installment != null && installment.get("month") != null) {
                installmentMonths = ((Number) installment.get("month")).intValue();
            }
        }

        return PortOnePaymentInfo.builder()
                .paymentId(paymentId)
                .transactionId(transactionId)
                .status(convertStatus(status))
                .amount(totalAmount)
                .method(convertMethod(method))
                .pgProvider(convertPgProvider(channel))
                .pgTxId(pgTxId)
                .receiptUrl(receiptUrl)
                .cardCompany(cardCompany)
                .cardNumber(cardNumber)
                .approveNo(approveNo)
                .installmentMonths(installmentMonths)
                .failCode(failure != null ? (String) failure.get("reason") : null)
                .failMessage(failure != null ? (String) failure.get("message") : null)
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

    private PaymentMethod convertMethod(Map<String, Object> method) {
        if (method == null) return null;
        String type = (String) method.get("type");
        if (type == null) return null;

        return switch (type) {
            case "PaymentMethodCard" -> PaymentMethod.CARD;
            case "PaymentMethodTransfer" -> PaymentMethod.TRANSFER;
            case "PaymentMethodVirtualAccount" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "PaymentMethodEasyPay" -> PaymentMethod.EASY_PAY;
            default -> null;  // MOBILE 등 ERD에 정의되지 않은 결제 수단은 null 처리
        };
    }

    private PgProvider convertPgProvider(Map<String, Object> channel) {
        if (channel == null) return null;
        String pgProvider = (String) channel.get("pgProvider");
        if (pgProvider == null) return null;

        return switch (pgProvider) {
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
