package com.finders.api.infra.oauth;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.infra.oauth.model.OAuthUserInfo;

public interface OAuthClient {
    SocialProvider provider();
    OAuthUserInfo getUserInfo(String accessToken);
    String getAccessToken(String code);
}
