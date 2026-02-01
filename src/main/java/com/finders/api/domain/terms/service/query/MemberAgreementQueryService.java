package com.finders.api.domain.terms.service.query;

import com.finders.api.domain.terms.enums.TermsType;

public interface MemberAgreementQueryService {

    boolean hasAgreedToTerms(Long memberId, TermsType type);
}
