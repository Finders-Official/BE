package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * TokenHistory Repository
 */
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {

    /**
     * 회원의 토큰 이력 조회 (최신순)
     */
    List<TokenHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 특정 회원 리스트에 포함된 모든 토큰 히스토리 정보 삭제
    @Modifying
    @Query("delete from TokenHistory t where t.member in :members")
    void deleteAllByMemberIn(@Param("members") List<MemberUser> members);
}
