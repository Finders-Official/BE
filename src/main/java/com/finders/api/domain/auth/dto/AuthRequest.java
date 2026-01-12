package com.finders.api.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    public record SocialLogin(
            @NotBlank String provider,

            @JsonProperty("accessToken")
            @NotBlank String accessToken
    ) {}


    public record SocialCodeLogin(
            @NotBlank String provider,
            @NotBlank String code
    ) {}

    public record TokenReissue(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {}
}
