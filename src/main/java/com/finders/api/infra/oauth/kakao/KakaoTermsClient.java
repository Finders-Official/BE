package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.oauth.OAuthTermsClient;
import com.finders.api.infra.oauth.kakao.dto.KakaoTermsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class KakaoTermsClient implements OAuthTermsClient {
    private final RestClient restClient;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public List<String> getAgreedTermsTags(String accessToken) {
        KakaoTermsResponse response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/service_terms")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.value() == 401, (req, res) -> {
                    log.warn("[KakaoTermsClient] access token 무효");
                    throw new CustomException(ErrorCode.KAKAO_ACCESS_TOKEN_INVALID);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("[KakaoTermsClient] 카카오 서버 오류");
                    throw new CustomException(ErrorCode.KAKAO_SERVER_ERROR);
                })
                .body(KakaoTermsResponse.class);

        if (response == null) {
            throw new CustomException(ErrorCode.KAKAO_TERMS_FETCH_FAILED);
        }

        if (response.serviceTerms() == null) {
            return List.of();
        }

        return response.serviceTerms().stream()
                .map(KakaoTermsResponse.ServiceTerm::tag)
                .toList();
    }
}
