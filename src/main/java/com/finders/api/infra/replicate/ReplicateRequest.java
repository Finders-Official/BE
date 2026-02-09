package com.finders.api.infra.replicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ReplicateRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreatePrediction(
            String version,
            Object input,
            String webhook,
            @JsonProperty("webhook_events_filter")
            List<String> webhookEventsFilter
    ) {
        public static CreatePrediction of(ReplicateModelInput modelInput, String webhookUrl) {
            String finalWebhook = (webhookUrl != null && webhookUrl.startsWith("https://")) ? webhookUrl : null;

            return new CreatePrediction(
                    modelInput.modelVersion(),
                    modelInput,
                    finalWebhook,
                    finalWebhook != null ? List.of("completed") : null
            );
        }
    }
}
