package com.finders.api.domain.auth.dto;

import com.finders.api.domain.member.enums.SocialProvider;

public record SignupTokenPayload(
        SocialProvider provider,
        String providerId,
        String name,
        String nickname,
        String profileImage,
        String email
) {}
