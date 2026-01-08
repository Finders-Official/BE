package com.finders.api.domain.auth.controller;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.service.SocialAuthService;
import com.finders.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/social/login")
    public ApiResponse<?> socialLogin(
            @Valid @RequestBody AuthRequest.SocialLogin request
    ) {
        return socialAuthService.SocialLogin(request);
    }
}
