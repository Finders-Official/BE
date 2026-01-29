package com.finders.api.domain.terms.repository;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.terms.entity.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {

    // 특정 회원 리스트에 포함된 모든 배송지 리스트 정보 삭제
    @Modifying
    @Query("delete from MemberAgreement ma where ma.member in :members")
    void deleteAllByMemberIn(@Param("members") List<MemberUser> members);
}
