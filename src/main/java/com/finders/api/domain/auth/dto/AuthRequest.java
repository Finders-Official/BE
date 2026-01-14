package com.finders.api.domain.auth.dto;

import jakarta.validation.constraints.Email;
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

    public record OwnerSignupRequest(
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String name,
            @NotBlank String phone,
            @NotBlank String businessNumber,
            @NotBlank String bankName,
            @NotBlank String bankAccountNumber,
            @NotBlank String bankAccountHolder
    ) {}

    public record OwnerLoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}
}
