package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.oauth.OAuthTermsClient;
import com.finders.api.infra.oauth.kakao.dto.KakaoTermsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${oauth2.kakao.admin-key}")
    private String adminKey;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public List<String> getAgreedTermsTags(String providerId) {
        KakaoTermsResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("kapi.kakao.com")
                        .path("/v1/user/service_terms")
                        .queryParam("target_id_type", "user_id")
                        .queryParam("target_id", providerId)
                        .build())
                .header("Authorization", "KakaoAK " + adminKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    log.error("[KakaoTermsClient.getAgreedTermsTags] 약관 조회 실패 - ID: {}", providerId);
                    throw new CustomException(ErrorCode.KAKAO_TERMS_FETCH_FAILED);
                })
                .body(KakaoTermsResponse.class); // 여기서 자동 매핑!

        if (response == null || response.allowedServiceTerms() == null) {
            return List.of();
        }

        // 태그 리스트만 추출해서 반환
        return response.allowedServiceTerms().stream()
                .map(KakaoTermsResponse.AllowedServiceTerm::tag)
                .toList();
    }
}
