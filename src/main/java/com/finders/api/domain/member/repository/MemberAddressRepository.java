package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberAddress;
import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    // 특정 유저의 배송지가 존재하는지 확인
    boolean existsByUserId(Long memberId);

    // 특정 유저의 기존 기본 배송지 찾기
    Optional<MemberAddress> findByUser_IdAndIsDefaultTrue(Long memberId);

    // 유저 id 기준 조회, 기본배송지 우선 -> 최신순 정렬
    List<MemberAddress> findAllByUser_IdOrderByIsDefaultDescCreatedAtDesc(Long memberId);

    // 특정 회원 리스트에 포함된 모든 배송지 리스트 정보 삭제
    @Modifying
    @Query("delete from MemberAddress a where a.user in :members")
    void deleteAllByMemberIn(@Param("members") List<MemberUser> members);
}
