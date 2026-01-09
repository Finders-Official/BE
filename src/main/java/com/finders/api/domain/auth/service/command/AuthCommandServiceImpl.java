package com.finders.api.domain.auth.service.command;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.dto.AuthResponse;
import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.SocialAccount;
import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.enums.MemberType;
import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.SocialAccountRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.JwtTokenProvider;
import com.finders.api.global.security.RefreshTokenHasher;
import com.finders.api.global.security.SignupTokenProvider;
import com.finders.api.infra.oauth.OAuthClient;
import com.finders.api.infra.oauth.OAuthClientFactory;
import com.finders.api.infra.oauth.model.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCommandServiceImpl implements AuthCommandService {

    private final OAuthClientFactory oAuthClientFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberUserRepository memberUserRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final SignupTokenProvider signupTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<?> socialLogin(AuthRequest.SocialLogin request) {
        SocialProvider provider = parseProvider(request.provider());

        OAuthClient client = oAuthClientFactory.getOAuthClient(provider);
        OAuthUserInfo userInfo = client.getUserInfo(request.accessToken());

        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderId(provider, userInfo.providerId())
                .orElse(null);

        // 신규 회원: signupToken 발급 -> 회원가입
        if (socialAccount == null) {
            SignupTokenPayload payload = new SignupTokenPayload(
                    provider,
                    userInfo.providerId(),
                    userInfo.name(),
                    userInfo.nickname(),
                    userInfo.profileImage(),
                    userInfo.email()
            );

            String signupToken = signupTokenProvider.createSignupToken(payload);

            AuthResponse.SignupRequired data = AuthResponse.SignupRequired.builder()
                    .isNewMember(true)
                    .signupToken(signupToken)
                    .socialProfile(AuthResponse.SocialProfile.builder()
                            .provider(provider.name())
                            .providerId(userInfo.providerId())
                            .name(userInfo.name())
                            .nickname(userInfo.nickname())
                            .profileImage(userInfo.profileImage())
                            .build())
                    .build();

            return ApiResponse.success(SuccessCode.AUTH_SIGNUP_REQUIRED, data);
        }

        // 기존 회원: 로그인 처리
        var user = socialAccount.getUser();

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (user.getRole() != MemberType.USER) {
            throw new CustomException(ErrorCode.AUTH_INVALID_ROLE);
        }

        // 계정 상태 확인
        if (user.getStatus() != MemberStatus.ACTIVE) {
            throw new CustomException(ErrorCode.MEMBER_INACTIVE);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenHasher.saveRefreshToken(user.getId(), refreshToken);

        AuthResponse.LoginSuccess data = AuthResponse.LoginSuccess.of(accessToken, refreshToken, user);

        return ApiResponse.success(SuccessCode.AUTH_LOGIN_SUCCESS, data);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
            MemberUser user = memberUserRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            if (passwordEncoder.matches(hashToken(refreshToken), user.getRefreshTokenHash())) {
                user.updateRefreshTokenHash(null);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private SocialProvider parseProvider(String provider) {
        try {
            return SocialProvider.valueOf(provider);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
