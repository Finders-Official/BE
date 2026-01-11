package com.finders.api.infra.oauth.model;

import com.finders.api.domain.member.enums.SocialProvider;
import lombok.Builder;

@Builder
public record OAuthUserInfo(
        SocialProvider provider,
        String providerId,
        String name,
        String nickname,
        String profileImage,
        String email
) {}
