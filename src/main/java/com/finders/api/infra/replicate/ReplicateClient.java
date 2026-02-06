package com.finders.api.infra.replicate;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Replicate API 클라이언트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplicateClient {

    @Qualifier("replicateWebClient")
    private final WebClient replicateWebClient;
    private final ReplicateProperties properties;

    public ReplicateResponse.Prediction createInpaintingPrediction(String imageUrl, String maskUrl) {
        log.info("[ReplicateClient.createInpaintingPrediction] Creating inpainting prediction");

        ReplicateRequest.CreatePrediction request = ReplicateRequest.CreatePrediction.of(
                properties.modelVersion(),
                imageUrl,
                maskUrl,
                properties.getWebhookUrl()
        );

        try {
            ReplicateResponse.Prediction response = replicateWebClient.post()
                    .uri("/predictions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(ReplicateResponse.Prediction.class)
                    .block();

            log.info("[ReplicateClient.createInpaintingPrediction] Prediction created: id={}, status={}",
                    response != null ? response.id() : null,
                    response != null ? response.status() : null);

            return response;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ReplicateClient.createInpaintingPrediction] Failed to create prediction: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, e);
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
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ReplicateClient.getPrediction] Failed to get prediction: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, e);
        }
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("[ReplicateClient.handleError] API error: status={}, body={}",
                            response.statusCode(), body);
                    return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR,
                            "Replicate API error: " + response.statusCode()));
                });
    }
}
