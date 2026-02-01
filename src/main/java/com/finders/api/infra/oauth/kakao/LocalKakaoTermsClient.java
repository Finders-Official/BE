package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.infra.oauth.OAuthTermsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("local")
public class LocalKakaoTermsClient implements OAuthTermsClient {

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public List<String> getAgreedTermsTags(String providerId) {
        log.info("[LocalKakaoTermsClient.getAgreedTermsTags] 로컬 모드로 가짜 약관 데이터를 반환합니다. ID: {}", providerId);

        return List.of(
                "service_policy",
                "privacy_policy",
                "required_notice",
                "marketing_info",
                "location_policy"
        );
    }
}