package com.finders.api.domain.auth.dto;

import com.finders.api.domain.member.enums.SocialProvider;

public record SignupTokenPayload(
        SocialProvider provider,
        String providerId,
        String accessToken,
        String name,
        String nickname,
        String profileImage,
        String email
) {}
