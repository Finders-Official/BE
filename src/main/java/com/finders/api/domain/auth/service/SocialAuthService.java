package com.finders.api.domain.auth.service;

import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.SocialAccountRepository;
import com.finders.api.global.security.JwtTokenProvider;
import com.finders.api.global.security.RefreshTokenHasher;
import com.finders.api.infra.oauth.OAuthClientFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthService {

    private final OAuthClientFactory oAuthClientFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberUserRepository memberUserRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
}
