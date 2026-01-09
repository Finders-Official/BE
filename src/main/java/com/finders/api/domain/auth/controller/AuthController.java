package com.finders.api.domain.auth.controller;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.service.command.AuthCommandService;
import com.finders.api.global.response.ApiResponse;
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
}
