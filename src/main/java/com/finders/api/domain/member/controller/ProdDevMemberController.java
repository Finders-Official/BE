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
@Profile("dev")
public class ProdDevMemberController {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${dev.secret-key:}")
    private String devSecretKey;


    @Operation(summary = "서버용 토큰 발급", description = "서버 환경에서는 반드시 SecretKey 헤더에 보안 키를 포함해야 합니다.")
    @GetMapping("/login")
    public ApiResponse<String> devLogin(
            @RequestHeader("SecretKey") String secretKey,
            @RequestParam Long memberId
    ) {
        // 보안 키 검증 (설정값이 비어있거나 틀리면 거부)
        if (devSecretKey.isEmpty() || !devSecretKey.equals(secretKey)) {
            return ApiResponse.error(ErrorCode.UNAUTHORIZED, "보안 키가 틀렸습니다.");
        }

        String token = jwtTokenProvider.createAccessToken(memberId, "USER");
        return ApiResponse.success(SuccessCode.OK, "AccessToken: " + token);
    }
}
