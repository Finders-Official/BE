package com.finders.api.domain.member.dto.response;

import com.finders.api.domain.member.entity.MemberUser;
import io.swagger.v3.oas.annotations.media.Schema;

public class MemberResponse {

    @Schema(description = "회원가입 완료 응답 객체")
    public record SignupResult(
            String accessToken,
            String refreshToken,
            MemberSummary member
    ) {}

    public record MemberSummary(
            Long memberId,
            String nickname
    ) {
        public static MemberSummary from(MemberUser user) {
            return new MemberSummary(user.getId(), user.getNickname());
        }
    }
}
