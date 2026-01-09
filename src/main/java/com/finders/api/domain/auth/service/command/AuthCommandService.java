package com.finders.api.domain.auth.service.command;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.dto.AuthResponse;
import com.finders.api.global.response.ApiResponse;

public interface AuthCommandService {
    ApiResponse<?> socialLogin(AuthRequest.SocialLogin request);

    // 토큰 재발급
    AuthResponse.TokenInfo reissueToken(String refreshToken);

    // 로그아웃
    void logout(String token);
}
