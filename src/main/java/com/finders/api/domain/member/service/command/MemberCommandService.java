package com.finders.api.domain.member.service.command;

import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.dto.request.MemberPhoneRequest;
import com.finders.api.domain.member.dto.request.MemberRequest;
import com.finders.api.domain.member.dto.response.MemberPhoneResponse;
import com.finders.api.domain.member.dto.response.MemberResponse;

public interface MemberCommandService {
    // 휴대폰 인증번호 요청
    MemberPhoneResponse.SentInfo sendPhoneVerificationCode(MemberPhoneRequest.SendCode request);

    // 휴대폰 인증번호 확인
    MemberPhoneResponse.VerificationResult verifyPhoneCode(MemberPhoneRequest.VerifyCode request, boolean isSignupFlow);

    // 소셜 회원가입 완료
    MemberResponse.SignupResult signupSocialComplete(SignupTokenPayload payload, MemberRequest.SocialSignupComplete request);
    
    // 내 정보 수정
    void updateProfile(Long memberId, MemberRequest.UpdateProfile request);
}
