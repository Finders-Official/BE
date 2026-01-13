package com.finders.api.infra.oauth;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuthClientFactory {
    private final List<OAuthClient> clients;

    public OAuthClient getOAuthClient(SocialProvider provider) {
        return clients.stream()
                .filter(c -> c.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER));
    }
}
