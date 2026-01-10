package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Replicate API 응답 DTO
 */
public class ReplicateResponse {

    /**
     * Prediction 응답 (API 응답 및 Webhook 페이로드 공용)
     */
    public record Prediction(
            String id,
            String status,
            List<String> output,
            String error,
            @JsonProperty("created_at")
            String createdAt
    ) {
        public boolean isCompleted() {
            return "succeeded".equals(status) || "failed".equals(status);
        }

        public boolean isSucceeded() {
            return "succeeded".equals(status);
        }

        public boolean isFailed() {
            return "failed".equals(status);
        }

        public String getFirstOutput() {
            if (output == null || output.isEmpty()) {
                return null;
            }
            return output.get(0);
        }
    }
}
