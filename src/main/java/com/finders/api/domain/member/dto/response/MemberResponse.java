package com.finders.api.domain.member.dto.response;

import com.finders.api.domain.member.entity.MemberUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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

    public record MyProfile(
            MemberInfo member,
            EditableInfo editable,
            RoleData roleData
    ) {}

    public record MemberInfo(
            Long memberId,
            String name,
            String phone,
            String role,
            String status
    ) {}

    public record EditableInfo(
            boolean nickname,
            boolean phone,
            boolean profileImage
    ) {}

    public record RoleData(
            UserDetail user,
            // TODO: 추후 owner, admin 추가 (현재는 null)
            Object owner,
            Object admin
    ) {}

    public record UserDetail(
            String nickname,
            String profileImage,
            Integer creditBalance,
            List<SocialAccountInfo> socialAccounts
    ) {}

    public record SocialAccountInfo(
            String provider,
            String email
    ) {}
}
