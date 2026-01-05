package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * TokenHistory Repository
 */
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {

    /**
     * 회원의 토큰 이력 조회 (최신순)
     */
    List<TokenHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
