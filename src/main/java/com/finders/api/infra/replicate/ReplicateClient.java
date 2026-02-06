package com.finders.api.infra.replicate;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ReplicateClient {

    private static final Logger log = LoggerFactory.getLogger(ReplicateClient.class);
    private final WebClient replicateWebClient;
    private final ReplicateProperties properties;

    public ReplicateClient(@Qualifier("replicateWebClient") WebClient replicateWebClient, ReplicateProperties properties) {
        this.replicateWebClient = replicateWebClient;
        this.properties = properties;
    }

    public ReplicateResponse.Prediction createInpaintingPrediction(String imageUrl, String maskUrl) {
        log.info("[ReplicateClient.createInpaintingPrediction] Creating prediction");
        
        ReplicateRequest.CreatePrediction request = ReplicateRequest.CreatePrediction.of(
                properties.modelVersion(),
                imageUrl,
                maskUrl,
                properties.getWebhookUrl()
        );

        try {
            return replicateWebClient.post()
                    .uri("/predictions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(ReplicateResponse.Prediction.class)
                    .block();

        } catch (Exception e) {
            log.error("[ReplicateClient.createInpaintingPrediction] Failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "Replicate API 호출 실패: " + e.getMessage());
        }
    }

    public ReplicateResponse.Prediction getPrediction(String predictionId) {
        log.debug("[ReplicateClient.getPrediction] Getting prediction: id={}", predictionId);
        try {
            return replicateWebClient.get()
                    .uri("/predictions/{id}", predictionId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(ReplicateResponse.Prediction.class)
                    .block();
        } catch (Exception e) {
            log.error("[ReplicateClient.getPrediction] Failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "Replicate 조회 실패: " + e.getMessage());
        }
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("[ReplicateClient.handleError] Error response: status={}, body={}", response.statusCode(), body);
                    return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR,
                            "Replicate API error: " + response.statusCode()));
                });
    }
}
