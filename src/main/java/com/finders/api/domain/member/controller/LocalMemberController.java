package com.finders.api.domain.member.controller;

import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.JwtTokenProvider;
import com.finders.api.global.security.SignupTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "개발용 도구", description = "개발 및 테스트를 위한 API")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile("local")
public class LocalMemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final SignupTokenProvider signupTokenProvider;
    private final MemberRepository memberRepository;

    @Operation(summary = "로컬 개발용 토큰 발급", description = "로컬 환경에서는 보안 키 없이 ID를 입력하여 토큰을 발급받습니다. 모든 role(USER, OWNER, ADMIN) 지원.")
    @GetMapping("/login")
    public ApiResponse<String> devLogin(
            @RequestParam Long memberId
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String token = jwtTokenProvider.createAccessToken(memberId, member.getRole().name());

        return ApiResponse.success(SuccessCode.OK, "AccessToken: " + token);
    }

    @Operation(
            summary = "로컬 개발용 SignupToken 발급",
            description = "로컬 개발 환경에서 OAuth 인증 없이 테스트용 SignupToken을 발급합니다."
    )
    @GetMapping("/signup-token")
    public ApiResponse<Map<String, String>> getMockSignupToken(
            @RequestParam(defaultValue = "KAKAO") SocialProvider provider,
            @RequestParam(defaultValue = "1") String providerId,
            @RequestParam(defaultValue = "test@kakao.com") String email,
            @RequestParam(defaultValue = "테스트유저") String name,
            @RequestParam(defaultValue = "파인더스") String nickname
    ) {
        SignupTokenPayload payload = new SignupTokenPayload(
                provider,
                providerId,
                "mock_access_token",
                name,
                nickname,
                "https://test-image.com",
                email
        );

        String token = signupTokenProvider.createSignupToken(payload);

        return ApiResponse.success(SuccessCode.OK, Map.of("signupToken", token));
    }
}