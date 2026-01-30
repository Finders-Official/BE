package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.entity.MemberUser;

public interface MemberUserQueryService {

    // 활성화된 회원 조회 (존재하지 않거나 탈퇴한 경우 예외 발생)
    MemberUser getActiveMember(Long memberId);
}
