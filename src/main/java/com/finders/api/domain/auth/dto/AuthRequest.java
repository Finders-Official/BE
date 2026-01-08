package com.finders.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    public record SocialLogin(
            @NotBlank String provider,
            @NotBlank String accessToken
    ) {}
}
