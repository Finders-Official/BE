package com.finders.api.domain.member.repository;

import com.finders.api.domain.terms.entity.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {

    @Query("""
            select (count(ma) > 0)
            from MemberAgreement ma
            where ma.member.id = :memberId
              and ma.terms.type = com.finders.api.domain.terms.enums.TermsType.LOCATION
              and ma.isAgreed = true
            """)
    boolean existsLocationAgreement(@Param("memberId") Long memberId);
}
