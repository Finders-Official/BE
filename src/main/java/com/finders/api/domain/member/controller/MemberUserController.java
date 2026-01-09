package com.finders.api.domain.member.controller;

import com.finders.api.domain.member.dto.response.MemberUserResponse;
import com.finders.api.domain.member.service.query.MemberQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원(Member)", description = "회원가입 완료, 휴대폰 본인 인증 및 사용자 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberUserController {

    private final MemberQueryService memberQueryService;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "사용하려는 닉네임이 이미 존재하는지 확인합니다."
    )
    @GetMapping("/nickname/check")
    public ApiResponse<MemberUserResponse.NicknameCheck> checkNickname(
            @RequestParam("nickname") @NotBlank String nickname
    ) {
        boolean isAvailable = memberQueryService.isNicknameAvailable(nickname);

        MemberUserResponse.NicknameCheck data = MemberUserResponse.NicknameCheck.of(nickname, isAvailable);

        return ApiResponse.success(SuccessCode.MEMBER_200, data);
    }
}
