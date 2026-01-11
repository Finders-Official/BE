package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.oauth.OAuthClient;
import com.finders.api.infra.oauth.model.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private final RestClient kakaoRestClient;

    @Value("${auth.mock.enabled:false}")
    private boolean isMockEnabled;

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        // 로컬 환경이고 특정 토큰일 때만 모킹 작동
        if (isMockEnabled && ("string".equals(accessToken) || "test_token".equals(accessToken))) {
            return OAuthUserInfo.builder()
                    .provider(SocialProvider.KAKAO)
                    .providerId("999999999") // DB에 없는 새로운 ID로 설정
                    .name("테스트유저")
                    .nickname("파인더스")
                    .profileImage("https://test-image.com")
                    .email("test@kakao.com")
                    .build();
        }

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
}
