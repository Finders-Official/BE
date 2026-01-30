package com.finders.api.infra.oauth.kakao;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.infra.oauth.OAuthUnlinkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class LocalKakaoUnlinkClient implements OAuthUnlinkClient {

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public void unlink(String providerId) {
        log.info("[LocalKakaoUnlinkClient.unlink] 연동 해제 요청 성공! (대상 ID: {})", providerId);
    }
}
