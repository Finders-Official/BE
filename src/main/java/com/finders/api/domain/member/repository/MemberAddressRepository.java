package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    // 특정 유저의 기존 기본 배송지 찾기
    Optional<MemberAddress> findByUserIdAndIsDefaultTrue(Long memberId);
}
