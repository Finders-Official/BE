package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.oauth.OAuthClient;
import com.finders.api.infra.oauth.model.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
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

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        // ------------------------------------------------------------------
        // 로컬 테스트용 모킹 (실제 카카오 서버 호출을 건너뜀)
        // ------------------------------------------------------------------
        if ("string".equals(accessToken) || "test_token".equals(accessToken)) {
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
            Map<?, ?> body = kakaoRestClient.get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            String providerId = String.valueOf(body.get("id"));

            // 1. 카카오 계정 정보 레이어 접근
            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            // 2. 닉네임 및 프로필 이미지 (우선순위: profile > properties)
            String nickname = (String) profile.getOrDefault("nickname", body.get("properties"));
            if (nickname == null) {
                Map<String, Object> properties = (Map<String, Object>) body.get("properties");
                nickname = (String) properties.get("nickname");
            }

            String profileImage = (String) profile.get("profile_image_url");

            // 3. 실명 및 이메일 추출
            String name = (String) kakaoAccount.get("name");
            String email = (String) kakaoAccount.get("email");

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
