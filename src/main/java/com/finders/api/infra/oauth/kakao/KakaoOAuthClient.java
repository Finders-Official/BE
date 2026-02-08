package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.oauth.OAuthClient;
import com.finders.api.infra.oauth.model.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private final RestClient kakaoRestClient;

    @Value("${auth.mock.enabled:false}")
    private boolean isMockEnabled;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    @Value("${oauth2.kakao.client-id}") private String clientId;
    @Value("${oauth2.kakao.client-secret}") private String clientSecret;
    @Value("${oauth2.kakao.redirect-uri}") private String redirectUri;

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserMeResponse response = kakaoRestClient.get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(KakaoUserMeResponse.class);

            if (response == null) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
            }

            String providerId = String.valueOf(response.id());
            var kakaoAccount = response.kakaoAccount();
            var profile = (kakaoAccount != null) ? kakaoAccount.profile() : null;
            var properties = response.properties();

            // 닉네임 추출 (우선순위: profile > properties)
            String nickname = null;
            if (profile != null) nickname = profile.nickname();
            if (nickname == null && properties != null) nickname = properties.nickname();

            // 프로필 이미지 추출
            String profileImage = (profile != null) ? profile.profileImageUrl() : null;

            // 실명 및 이메일 추출
            String name = (kakaoAccount != null) ? kakaoAccount.name() : null;
            String email = (kakaoAccount != null) ? kakaoAccount.email() : null;

            return OAuthUserInfo.builder()
                    .provider(SocialProvider.KAKAO)
                    .providerId(providerId)
                    .name(name != null ? name : nickname) // 실명 없으면 닉네임으로 대체
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .email(email) // DTO에 추가
                    .build();

        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    public String getAccessToken(String code) {
        try {
            KakaoTokenResponse response = RestClient.create().post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(createTokenRequestBody(code))
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
            }
            return response.accessToken();
        } catch (RestClientResponseException e) {
            log.error("카카오 토큰 요청 실패: {}", e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "카카오 토큰 요청 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("카카오 토큰 요청 중 알 수 없는 에러 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private MultiValueMap<String, String> createTokenRequestBody(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        return params;
    }
}
