package com.finders.api.domain.member.service.command;

public interface MemberUserCommandService {
    // 회원 탈퇴
    void withdraw(Long memberId);
}
