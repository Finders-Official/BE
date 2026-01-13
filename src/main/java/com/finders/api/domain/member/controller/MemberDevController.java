package com.finders.api.domain.member.controller;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로컬 개발용 토큰 발급", description = "로컬 개발용 토큰 발급 API")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile("local") // 실제 서버(prod)에서는 작동하지 않도록 보호
public class MemberDevController {

    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "로컬 개발용 토큰 발급", description = "ID만 입력하면 해당 유저의 Bearer 토큰을 반환합니다.")
    @GetMapping("/login")
    public ApiResponse<String> devLogin(@RequestParam Long memberId) {
        String token = jwtTokenProvider.createAccessToken(memberId, "USER");
        return ApiResponse.success(SuccessCode.OK, "AccessToken: " + token);
    }
}