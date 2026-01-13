package com.finders.api.domain.auth.service.command;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.dto.AuthResponse;
import com.finders.api.global.response.ApiResponse;

public interface AuthCommandService {
    // 소셜 로그인
    ApiResponse<?> socialLogin(AuthRequest.SocialLogin request);
    ApiResponse<?> processSocialCodeLogin(AuthRequest.SocialCodeLogin request);

    // 토큰 재발급
    AuthResponse.TokenInfo reissueToken(String refreshToken);

    // 로그아웃
    void logout(String token);
}
