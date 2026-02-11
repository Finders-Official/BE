package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Replicate API 응답 DTO
 */
public class ReplicateResponse {

    /**
     * Prediction 응답 (API 응답 및 Webhook 페이로드 공용)
     * <p>
     * output 필드는 모델에 따라 형식이 다릅니다:
     * - List&lt;String&gt;: flux-kontext-apps 등 (예: ["url1", "url2"])
     * - String: cjwbw/supir-v0q 등 (예: "https://...")
     * <p>
     * Object로 받아 getFirstOutput()에서 타입에 따라 분기 처리합니다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Prediction(
            String id,
            String status,
            Object output,
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

        /**
         * 첫 번째 output URL을 반환합니다.
         * <p>
         * 모델별 output 형식 차이를 처리합니다:
         * - String 타입: 해당 값을 그대로 반환
         * - List 타입: 첫 번째 요소를 반환
         */
        @SuppressWarnings("unchecked")
        public String getFirstOutput() {
            if (output == null) {
                return null;
            }
            if (output instanceof String s) {
                return s;
            }
            if (output instanceof List<?> list) {
                if (list.isEmpty()) {
                    return null;
                }
                Object first = list.getFirst();
                return first != null ? first.toString() : null;
            }
            return output.toString();
        }
    }
}
