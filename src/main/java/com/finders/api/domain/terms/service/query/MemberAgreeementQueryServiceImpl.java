package com.finders.api.domain.terms.service.query;

import com.finders.api.domain.terms.enums.TermsType;
import com.finders.api.domain.terms.repository.MemberAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberAgreeementQueryServiceImpl implements MemberAgreementQueryService{
    
    private final MemberAgreementRepository memberAgreementRepository;

    @Override
    public boolean hasAgreedToTerms(Long memberId, TermsType type) {
        return memberAgreementRepository.existsByMember_IdAndTerms_TypeAndIsAgreed(memberId, type, true);
    }
}
