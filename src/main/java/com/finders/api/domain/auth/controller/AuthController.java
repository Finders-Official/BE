package com.finders.api.domain.auth.controller;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.dto.AuthResponse;
import com.finders.api.domain.auth.service.command.AuthCommandService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증(Auth)", description = "인증 및 토큰 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandService authCommandService;

    @Operation(
            summary = "소셜 로그인",
            description = "유저용 소셜 로그인 요청입니다."
    )
    @PostMapping("/social/login")
    public ApiResponse<?> socialLogin(
            @Valid @RequestBody AuthRequest.SocialLogin request
    ) {
        return authCommandService.socialLogin(request);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @PostMapping("/reissue")
    public ApiResponse<AuthResponse.TokenInfo> reissue(
            @Valid @RequestBody AuthRequest.TokenReissue request
    ) {
        AuthResponse.TokenInfo data = authCommandService.reissueToken(request.refreshToken());
        return ApiResponse.success(SuccessCode.AUTH_TOKEN_REFRESHED, data); // "토큰이 갱신되었습니다."
    }

    @Operation(
            summary = "로그아웃",
            description = "RefreshToken을 무효화해 로그아웃 처리합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody AuthRequest.TokenReissue request
    ) {
        authCommandService.logout(request.refreshToken());
        return ApiResponse.success(SuccessCode.AUTH_LOGOUT_SUCCESS, null);
    }
}
