package com.finders.api.domain.community.service.command;

import com.finders.api.domain.member.entity.MemberUser;

public interface SearchHistoryCommandService {
    void saveSearchHistory(MemberUser memberUser, String keyword);

    void deleteSearchHistory(Long historyId, Long memberId);

    void deleteAllSearchHistory(Long memberId);
}
