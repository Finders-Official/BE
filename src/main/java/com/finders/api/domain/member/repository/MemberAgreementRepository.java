package com.finders.api.domain.member.repository;

import com.finders.api.domain.terms.entity.MemberAgreement;
import com.finders.api.domain.terms.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {

    boolean existsByMember_IdAndTerms_TypeAndIsAgreed(Long memberId, TermsType type, boolean isAgreed);
}
