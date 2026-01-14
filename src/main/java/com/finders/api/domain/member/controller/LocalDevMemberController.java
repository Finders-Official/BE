package com.finders.api.domain.member.controller;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Tag(name = "개발용 도구", description = "개발 및 테스트를 위한 API")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile("local")
public class LocalDevMemberController {

    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "로컬 개발용 토큰 발급", description = "로컬 환경에서는 보안 키 없이 ID와 Role(USER/OWNER)을 입력하여 토큰을 발급받습니다.")
    @GetMapping("/login")
    public ApiResponse<String> devLogin(
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "USER") String role // 기본값을 USER로 설정
    ) {
        // role 값을 대문자로 변환하여 전달 (USER, OWNER 등)
        String token = jwtTokenProvider.createAccessToken(memberId, role.toUpperCase());
        return ApiResponse.success(SuccessCode.OK, "AccessToken: " + token);
    }
}