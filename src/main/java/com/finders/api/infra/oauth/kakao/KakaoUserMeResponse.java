package com.finders.api.infra.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserMeResponse(
        Long id,
        Properties properties,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record Properties(
            String nickname,
            @JsonProperty("profile_image") String profileImage
    ) { }

    public record KakaoAccount(
            Profile profile,
            String name,
            String email
    ) {
        public record Profile(
                String nickname,
                @JsonProperty("profile_image_url") String profileImageUrl
        ) { }
    }
}
