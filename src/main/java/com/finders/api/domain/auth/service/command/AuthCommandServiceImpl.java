package com.finders.api.domain.auth.service.command;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.dto.AuthResponse;
import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.SocialAccount;
import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.enums.MemberType;
import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.domain.member.repository.MemberOwnerRepository;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.SocialAccountRepository;
import com.finders.api.domain.member.service.command.MemberCommandService;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCommandServiceImpl implements AuthCommandService {

    private final OAuthClientFactory oAuthClientFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final MemberUserRepository memberUserRepository;
    private final MemberOwnerRepository memberOwnerRepository;

    private final MemberCommandService memberCommandService;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final SignupTokenProvider signupTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 프론트엔드가 준 AccessToken으로 로그인
    @Override
    @Transactional
    public ApiResponse<?> socialLogin(AuthRequest.SocialLogin request) {
        SocialProvider provider = parseProvider(request.provider());

        OAuthClient client = oAuthClientFactory.getOAuthClient(provider);
        OAuthUserInfo userInfo = client.getUserInfo(request.accessToken());

        return resolveSocialLogin(provider, userInfo);
    }

    // 카카오 인가 코드로 로그인
    @Override
    @Transactional
    public ApiResponse<?> processSocialCodeLogin(AuthRequest.SocialCodeLogin request) {
        SocialProvider provider = parseProvider(request.provider());

        OAuthClient client = oAuthClientFactory.getOAuthClient(provider);

        String socialAccessToken = client.getAccessToken(request.code());
        OAuthUserInfo userInfo = client.getUserInfo(socialAccessToken);

        // 공통 로그인 로직 실행
        return resolveSocialLogin(provider, userInfo);
    }

    // 신규/기존 회원 판별 및 응답 생성
    private ApiResponse<?> resolveSocialLogin(SocialProvider provider, OAuthUserInfo userInfo) {
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

            AuthResponse.SignupRequired data = new AuthResponse.SignupRequired(
                    true,
                    signupToken,
                    new AuthResponse.SocialProfile(
                            provider.name(),
                            userInfo.providerId(),
                            userInfo.name(),
                            userInfo.nickname(),
                            userInfo.profileImage()
                    )
            );

            return ApiResponse.success(SuccessCode.AUTH_SIGNUP_REQUIRED, data);
        }

        // 기존 회원: 로그인 처리
        MemberUser user = socialAccount.getUser();

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
    public AuthResponse.TokenInfo reissueToken(String refreshToken) {
        try {
            Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);

            MemberUser user = memberUserRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            if(!refreshTokenHasher.matches(refreshToken, user.getRefreshTokenHash())) {
                throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
            }

            // 새로운 AccessToken, RefreshToken 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(memberId, user.getRole().name());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(memberId);

            refreshTokenHasher.saveRefreshToken(memberId, newRefreshToken);

            long expiresIn = jwtTokenProvider.getAccessTokenExpiryMs() / 1000;

            return new AuthResponse.TokenInfo(
                    newAccessToken,
                    newRefreshToken, // 이 필드가 추가되어야 합니다.
                    expiresIn
            );

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new CustomException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
            MemberUser user = memberUserRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            if (refreshTokenHasher.matches(refreshToken, user.getRefreshTokenHash())) {
                user.updateRefreshTokenHash(null);
            }
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    @Override
    @Transactional
    public AuthResponse.OwnerSignupResponse signupOwner(AuthRequest.OwnerSignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        MemberOwner savedOwner = memberCommandService.saveMemberOwner(request, encodedPassword);

        return AuthResponse.OwnerSignupResponse.from(savedOwner);
    }

    @Override
    @Transactional
    public AuthResponse.OwnerLoginResponse loginOwner(AuthRequest.OwnerLoginRequest request) {
        MemberOwner owner = memberOwnerRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), owner.getPasswordHash())) {
            throw new CustomException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(owner.getId(), "OWNER");
        String refreshToken = jwtTokenProvider.createRefreshToken(owner.getId());

        refreshTokenHasher.saveRefreshToken(owner.getId(), refreshToken);

        return AuthResponse.OwnerLoginResponse.of(accessToken, refreshToken, owner);
    }

    private SocialProvider parseProvider(String provider) {
        try {
            return SocialProvider.valueOf(provider);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
        }
    }
}
