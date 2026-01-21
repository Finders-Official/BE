package com.finders.api.domain.community.service.command;

public interface SearchHistoryCommandService {
    void saveSearchHistory(Long memberId, String keyword);

    void deleteSearchHistory(Long historyId, Long memberId);

    void deleteAllSearchHistory(Long memberId);
}
