package com.finders.api.infra.oauth;

import com.finders.api.domain.member.enums.SocialProvider;

public interface OAuthUnlinkClient {
    SocialProvider getProvider();
    void unlink(String providerId);
}
