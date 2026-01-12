package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.dto.request.MemberRequest;
import com.finders.api.domain.member.dto.response.MemberResponse;
import com.finders.api.domain.member.entity.Member;

public interface MemberQueryService {

    boolean isNicknameAvailable(String nickname);

    // 마이페이지 조회
    MemberResponse.MyProfile getMyProfile(Long memberId);
    MemberResponse.MyProfile getMyProfile(Member member);
}
