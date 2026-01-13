package com.finders.api.domain.auth.dto;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.MemberUser;
import lombok.Builder;

public class AuthResponse {

    public record LoginSuccess(
            String accessToken,
            String refreshToken,
            MemberSummary member
    ) {
        public static LoginSuccess of(String accessToken, String refreshToken, Member member) {
            return new LoginSuccess(accessToken, refreshToken, MemberSummary.from(member));
        }
    }

    public record MemberSummary(
            Long id,
            String nickname
    ) {
        public static MemberSummary from(Member member) {
            String nickname = "파인더스";  // 기본값

            if (member instanceof MemberUser user) {
                nickname = user.getNickname();
            } else {
                nickname = member.getName();
            }

            return new MemberSummary(member.getId(), nickname);
        }
    }

    public record SignupRequired(
            boolean isNewMember,
            String signupToken,
            SocialProfile socialProfile
    ) {}

    @Builder
    public record SocialProfile(
            String provider,
            String providerId,
            String name,
            String nickname,
            String profileImage
    ) {}

    public record TokenInfo(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn
    ) {}
}
