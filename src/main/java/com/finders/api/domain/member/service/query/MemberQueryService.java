package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.dto.request.MemberRequest;
import com.finders.api.domain.member.dto.response.MemberResponse;
import com.finders.api.domain.member.entity.Member;

public interface MemberQueryService {

    boolean isNicknameAvailable(String nickname);

    boolean hasAgreedToTerms(Long memberId, com.finders.api.domain.terms.enums.TermsType type);

    // 마이페이지 조회
    MemberResponse.MyProfile getMyProfile(Long memberId);
    MemberResponse.MyProfile getMyProfile(Member member);
}
