package com.finders.api.domain.member.controller;

import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.dto.request.MemberPhoneRequest;
import com.finders.api.domain.member.dto.request.MemberRequest;
import com.finders.api.domain.member.dto.response.MemberPhoneResponse;
import com.finders.api.domain.member.dto.response.MemberResponse;
import com.finders.api.domain.member.service.command.MemberCommandService;
import com.finders.api.domain.member.service.query.MemberQueryService;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원(Member)", description = "회원가입 완료, 휴대폰 본인 인증 및 사용자 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @Operation(
            summary = "휴대폰 인증번호 요청",
            description = "입력한 번호로 6자리 인증번호를 발송합니다. <br>" +
                    "**[권한 안내]** <br>" +
                    "- purpose가 **SIGNUP**인 경우: SignupToken(GUEST) 필요 <br>" +
                    "- purpose가 **MY_PAGE**인 경우: AccessToken(USER) 필요"
    )
    @PostMapping("/phone/verify/request")
    public ApiResponse<MemberPhoneResponse.SentInfo> sendVerifyCode(
            @AuthenticationPrincipal Object principal,
            @RequestBody @Valid MemberPhoneRequest.SendCode request
    ) {
        validateTokenPurpose(principal, request.purpose());

        MemberPhoneResponse.SentInfo data = memberCommandService.sendPhoneVerificationCode(request);

        return ApiResponse.success(SuccessCode.AUTH_PHONE_CODE_SENT, data);
    }

    @Operation(
            summary = "휴대폰 인증번호 확인",
            description = "발송된 인증번호를 검증합니다."
    )
    @PostMapping("/phone/verify/confirm")
    public ApiResponse<MemberPhoneResponse.VerificationResult> verifyPhoneCode(
            @AuthenticationPrincipal Object principal,
            @RequestBody @Valid MemberPhoneRequest.VerifyCode request
    ) {
        if (principal == null) throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);

        String authority = "";
        if (principal instanceof User user) {
            authority = user.getAuthorities().iterator().next().getAuthority();
        } else if (principal instanceof SignupTokenPayload) {
            authority = "ROLE_GUEST";
        }

        boolean isSignupFlow = "ROLE_GUEST".equals(authority);

        return ApiResponse.success(SuccessCode.AUTH_PHONE_VERIFIED,
                memberCommandService.verifyPhoneCode(request, isSignupFlow));
    }

    private void validateTokenPurpose(Object principal, String purpose) {
        if (principal == null) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        String authority = "";

        // 1. Principal 타입에 따라 권한(Role) 추출
        if (principal instanceof User user) {
            // JwtTokenProvider를 통해 들어온 경우 (AccessToken)
            authority = user.getAuthorities().iterator().next().getAuthority();
        }
        else if (principal instanceof SignupTokenPayload) {
            // SignupTokenProvider를 통해 들어온 경우 (SignupToken)
            // SignupTokenProvider.getAuthentication에서 ROLE_GUEST를 넣어주므로 이를 활용합니다.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            authority = auth.getAuthorities().iterator().next().getAuthority();
        }

        // 2. 목적(Purpose)에 따른 권한 검증
        // 회원가입 단계(SIGNUP)인데 GUEST 권한이 없는 경우
        if ("SIGNUP".equals(purpose) && !"ROLE_GUEST".equals(authority)) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        // 서비스 이용 단계(MY_PAGE 등)인데 USER 권한이 없는 경우
        if ("MY_PAGE".equals(purpose) && !"ROLE_USER".equals(authority)) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    @Operation(
            summary = "소셜 회원가입 완료",
            description = "휴대폰 인증 완료 후, 닉네임과 약관 동의를 받아 회원가입을 최종 완료합니다. <br>" +
                    "헤더에 로그인 API에서 받은 **SignupToken(GUEST 권한)**을 `Authorization` 헤더에 담아 보내야 합니다."
    )
    @PostMapping("/social/signup/complete")
    public ApiResponse<MemberResponse.SignupResult> socialSignupComplete(
            @AuthenticationPrincipal SignupTokenPayload payload,
            @RequestBody @Valid MemberRequest.SocialSignupComplete request
    ) {
        MemberResponse.SignupResult data = memberCommandService.signupSocialComplete(payload, request);

        return ApiResponse.success(SuccessCode.MEMBER_CREATED, data);
    }

    @Operation(
            summary = "마이페이지(내 정보) 조회",
            description = "로그인한 사용자의 기본 정보 및 프로필 데이터를 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<MemberResponse.MyProfile> getMyProfile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        MemberResponse.MyProfile profile = memberQueryService.getMyProfile(authUser.memberId());
        return ApiResponse.success(SuccessCode.MEMBER_ME_FOUND, profile);
    }
}
