package com.finders.api.domain.member.controller;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.enums.MemberType;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.global.exception.CustomException;
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
    private final MemberRepository memberRepository;

    @Operation(summary = "로컬 개발용 토큰 발급", description = "로컬 환경에서는 보안 키 없이 ID를 입력하여 토큰을 발급받습니다.")
    @GetMapping("/login")
    public ApiResponse<String> devLogin(
            @RequestParam Long memberId
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() != MemberType.USER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String token = jwtTokenProvider.createAccessToken(memberId, "USER");

        return ApiResponse.success(SuccessCode.OK, "AccessToken: " + token);
    }
}