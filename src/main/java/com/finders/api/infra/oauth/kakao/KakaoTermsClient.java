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
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("[KakaoTermsClient.getAgreedTermsTags] 약관 조회 실패");
                    throw new CustomException(ErrorCode.KAKAO_TERMS_FETCH_FAILED);
                })
                .body(KakaoTermsResponse.class);

        if (response == null || response.serviceTerms() == null) {
            return List.of();
        }

        return response.serviceTerms().stream()
                .map(KakaoTermsResponse.ServiceTerm::tag)
                .toList();
    }
}
