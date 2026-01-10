package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
