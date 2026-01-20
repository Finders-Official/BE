package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.SearchHistory;
import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findAllByMemberUserOrderByUpdatedAtDesc(MemberUser memberUser);

    Optional<SearchHistory> findByMemberUserAndKeyword(MemberUser memberUser, String keyword);

    void deleteAllByMemberUser(MemberUser memberUser);
}
