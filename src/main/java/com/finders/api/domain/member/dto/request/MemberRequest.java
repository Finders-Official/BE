package com.finders.api.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class MemberRequest {

    @Schema(description = "소셜 회원가입 완료 요청 객체")
    public record SocialSignupComplete(
            @Schema(description = "닉네임 (2~8자, 한글/영문/숫자)", example = "파인더")
            @NotBlank(message = "닉네임은 필수입니다.") @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9]{2,8}$",
                    message = "닉네임은 2~8자의 한글, 영문, 숫자로만 구성되어야 합니다.")
            String nickname,

            @Schema(description = "휴대폰 번호", example = "01012345678")
            @NotBlank String phone,

            @Schema(description = "휴대폰 인증 완료 증빙 토큰", example = "vpt_2b7c1d9a0f")
            @NotBlank String verifiedPhoneToken,

            @Schema(description = "약관 동의 리스트")
            @NotEmpty List<AgreementRequest> agreements
    ) {}

    public record AgreementRequest(
            @NotNull Long termsId,
            @NotNull Boolean isAgreed
    ) {}
}
