package com.finders.api.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

public class MemberPhoneResponse {

    // 요청 응답
    public record SentInfo(
            @Schema(description = "인증 요청 식별 ID (확인 API 호출 시 필요)", example = "6f0a1c7b-3f9a-4d4f-9b2f-1df2c2c2f1a1")
            String requestId,

            @Schema(description = "인증번호 유효시간 (초 단위)", example = "180")
            Integer expiresIn
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerificationResult(
            @Schema(description = "[USER 전용] 휴대폰 인증 성공 여부", example = "true")
            Boolean phoneVerified,

            @Schema(description = "[GUEST 전용] 회원가입 완료 시 제출할 증빙 토큰", example = "vpt_2b7c1d9a0f")
            String verifiedPhoneToken,

            @Schema(description = "[GUEST 전용] 인증된 휴대폰 번호", example = "01012345678")
            String phone,

            @Schema(description = "[GUEST 전용] 증빙 토큰 유효시간 (초 단위)", example = "600")
            Integer expiresIn
    ) {
        // 회원가입용
        public static VerificationResult signup(String token, String phone, int expires) {
            return new VerificationResult(true, token, phone, expires);
        }
        // 마이페이지용
        public static VerificationResult myPage(boolean verified, String phone) {
            return new VerificationResult(verified, null, phone, null);
        }
    }
}
