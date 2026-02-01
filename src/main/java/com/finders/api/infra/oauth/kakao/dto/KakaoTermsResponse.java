package com.finders.api.infra.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoTermsResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("allowed_service_terms") List<AllowedServiceTerm> allowedServiceTerms
) {
    public record AllowedServiceTerm(
            @JsonProperty("tag") String tag,
            @JsonProperty("agreed_at") String agreedAt
    ) {}
}
