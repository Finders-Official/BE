package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    // 특정 유저의 기존 기본 배송지 찾기
    Optional<MemberAddress> findByUser_IdAndIsDefaultTrue(Long memberId);

    // 유저 id 기준 조회, 기본배송지 우선 -> 최신순 정렬
    List<MemberAddress> findAllByUser_IdOrderByIsDefaultDescCreatedAtDesc(Long memberId);
}
