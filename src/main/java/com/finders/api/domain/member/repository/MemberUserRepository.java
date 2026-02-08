package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.MemberStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberUserRepository extends JpaRepository<MemberUser, Long> {
    /**
     * 닉네임으로 회원 조회
     */
    Optional<MemberUser> findByNickname(String nickname);

    /**
     * 닉네임 중복 확인
     */
    boolean existsByNickname(String nickname);

    /**
     * 토큰 차감/추가를 위한 배타적 락
     * <p>
     * 동시성 제어를 위해 PESSIMISTIC_WRITE 락을 사용합니다.
     * 토큰 잔액 체크 후 차감/추가 사이의 race condition을 방지합니다.
     * <p>
     * 락 타임아웃: 3초 (데드락 방지)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT m FROM MemberUser m WHERE m.id = :id")
    Optional<MemberUser> findByIdWithLock(@Param("id") Long id);

    // 상태가 WITHDRAWN이고, 삭제된 지 특정 시점이 지난 회원 목록 조회
    List<MemberUser> findAllByStatusAndDeletedAtBefore(MemberStatus status, LocalDateTime threshold);

    // 활성 유저 중, 토큰이 5개 미만인 유저만 토큰 1개 추가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberUser m SET m.tokenBalance = m.tokenBalance + 1 " +
            "WHERE m.tokenBalance < 5 AND m.status = 'ACTIVE'")
    int bulkRechargeTokens();
}
