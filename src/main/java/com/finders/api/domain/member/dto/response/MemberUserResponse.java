package com.finders.api.domain.member.dto.response;

public class MemberUserResponse {

    // 닉네임 체크 응답 DTO
    public record NicknameCheck(
            String nickname,
            Boolean available
    ) {
        public static NicknameCheck of(String nickname, boolean available) {
            return new NicknameCheck(nickname, available);
        }
    }
}
