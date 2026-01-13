package com.finders.api.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MemberPhoneRequest {

    // 인증번호 요청
    public record SendCode(
            @Schema(description = "휴대폰 번호 (하이픈 제외)", example = "01012345678")
            @NotBlank String phone,

            @Schema(description = "인증 목적", allowableValues = {"SIGNUP", "MY_PAGE"})
            @NotNull String purpose  // "SIGNUP" 또는 "MY_PAGE"
    ) {}

    // 인증번호 확인
    public record VerifyCode(
            @Schema(description = "인증번호 요청 시 발급받은 요청 ID", example = "6f0a1c7b-3f9a-4d4f-9b2f-1df2c2c2f1a1")
            @NotBlank String requestId,
            @NotBlank String code
    ) {}
}
