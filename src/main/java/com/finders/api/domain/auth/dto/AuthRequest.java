package com.finders.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    public record SocialLogin(
            @NotBlank(message = "소셜 프로바이더는 필수입니다.") String provider,
            @NotBlank(message = "accessToken은 필수입니다.") String accessToken
    ) {}


    public record SocialCodeLogin(
            @NotBlank(message = "소셜 프로바이더는 필수입니다.") String provider,
            @NotBlank(message = "인가 코드는 필수입니다.") String code
    ) {}

    public record TokenReissue(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {}
}
