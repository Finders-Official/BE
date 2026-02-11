package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.CreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * CreditHistory Repository
 */
public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {

    /**
     * 회원의 크레딧 이력 조회 (최신순)
     */
    List<CreditHistory> findByUserIdOrderByCreatedAtDesc(Long memberId);

    // 특정 회원 리스트에 포함된 모든 크레딧 히스토리 정보 삭제
    @Modifying
    @Query("delete from CreditHistory t where t.user in :users")
    void deleteAllByUserIn(@Param("users") List<MemberUser> users);
}
