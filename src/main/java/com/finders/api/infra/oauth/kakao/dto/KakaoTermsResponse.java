package com.finders.api.infra.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoTermsResponse(
        @JsonProperty("service_terms")
        List<ServiceTerm> serviceTerms

) {
    public record ServiceTerm(
            @JsonProperty("tag") String tag,
            @JsonProperty("agreed_at") String agreedAt
    ) {}
}