package com.finders.api.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    public record SocialLogin(
            @NotBlank String provider,

            @JsonProperty("accessToken")
            @NotBlank String accessToken
    ) {}
}
