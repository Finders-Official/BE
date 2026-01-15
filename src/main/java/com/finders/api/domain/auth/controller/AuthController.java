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
            summary = "소셜 로그인(모바일 Native SDK)",
            description = "유저용 소셜 로그인 요청입니다."
    )
    @PostMapping("/social/login")
    public ApiResponse<?> socialLogin(
            @Valid @RequestBody AuthRequest.SocialLogin request
    ) {
        return authCommandService.socialLogin(request);
    }

    @Operation(
            summary = "소셜 로그인 (웹 브라우저)",
            description = "유저용 소셜 로그인 요청입니다."
    )
    @PostMapping("/social/login/code") // GET -> POST로 변경
    public ApiResponse<?> socialCodeLogin(
            @Valid @RequestBody AuthRequest.SocialCodeLogin request
    ) {
        return authCommandService.processSocialCodeLogin(request);
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

    @Operation(
            summary = "사장님 회원가입",
            description = "이메일과 사업자 정보를 통해 회원가입합니다."
    )
    @PostMapping("/owner/signup")
    public ApiResponse<AuthResponse.OwnerSignupResponse> signupOwner(
            @RequestBody @Valid AuthRequest.OwnerSignupRequest request
    ) {
        AuthResponse.OwnerSignupResponse response = authCommandService.signupOwner(request);
        return ApiResponse.success(SuccessCode.MEMBER_CREATED, response);
    }

    @Operation(
            summary = "사장님 로그인",
            description = "이메일과 비밀번호로 로그인합니다."
    )
    @PostMapping("/owner/login")
    public ApiResponse<AuthResponse.OwnerLoginResponse> loginOwner(
            @RequestBody @Valid AuthRequest.OwnerLoginRequest request
    ) {
        AuthResponse.OwnerLoginResponse response = authCommandService.loginOwner(request);
        return ApiResponse.success(SuccessCode.AUTH_LOGIN_SUCCESS, response);
    }
}
