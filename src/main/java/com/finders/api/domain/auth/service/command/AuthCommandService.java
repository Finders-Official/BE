package com.finders.api.domain.auth.service.command;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.global.response.ApiResponse;

public interface AuthCommandService {
    ApiResponse<?> socialLogin(AuthRequest.SocialLogin request);

    // 로그아웃
    void logout(String token);
}
