package com.finders.api.infra.oauth;

import com.finders.api.domain.member.enums.SocialProvider;

import java.util.List;

public interface OAuthTermsClient {
    SocialProvider getProvider();
    List<String> getAgreedTermsTags(String providerId);
}
