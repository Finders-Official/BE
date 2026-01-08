package com.finders.api.infra.oauth.kakao;

public record KakaoUserMeResponse(
        Long id,
        Properties properties,
        KakaoAccount kakao_account
) {
    public record Properties(String nickname, String profiel_images) { }
    public record KakaoAccount(String email) {}
}
