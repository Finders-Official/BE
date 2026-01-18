package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberUser;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

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
}
