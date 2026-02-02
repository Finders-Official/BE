package com.finders.api.domain.terms.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.domain.terms.entity.MemberAgreement;
import com.finders.api.domain.terms.entity.Terms;
import com.finders.api.domain.terms.entity.TermsSocialMapping;
import com.finders.api.domain.terms.repository.MemberAgreementRepository;
import com.finders.api.domain.terms.repository.TermsRepository;
import com.finders.api.domain.terms.repository.TermsSocialMappingRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberAgreementCommandServiceImpl implements MemberAgreementCommandService {

    private final MemberAgreementRepository memberAgreementRepository;
    private final TermsRepository termsRepository;
    private final TermsSocialMappingRepository termsSocialMappingRepository;

    @Override
    public void saveAgreementsFromSocial(MemberUser user, SocialProvider provider, List<String> socialTags) {
        // 매핑 테이블에서 해당 소셜과 태그에 맞는 우리 약관(Terms)을 찾기
        List<Terms> matchedTerms = termsSocialMappingRepository.findAllActiveByProviderAndSocialTagIn(provider, socialTags)
                .stream()
                .map(TermsSocialMapping::getTerms)
                .toList();

        // 필수 약관 누락 여부 확인
        validateMandatoryTerms(matchedTerms);

        // 동의 여부 저장
        List<MemberAgreement> agreements = matchedTerms.stream()
                .map(term -> MemberAgreement.builder()
                        .member(user)
                        .terms(term)
                        .isAgreed(true)
                        .agreedAt(LocalDateTime.now())
                        .build())
                .toList();

        memberAgreementRepository.saveAll(agreements);
    }

    private void validateMandatoryTerms(List<Terms> agreedTerms) {
        List<Terms> mandatoryTerms = termsRepository.findAllByIsRequiredTrueAndIsActiveTrue();

        boolean allMandatoryAgreed = agreedTerms.containsAll(mandatoryTerms);

        if (!allMandatoryAgreed) {
            throw new CustomException(ErrorCode.AUTH_TERMS_NOT_AGREED);
        }
    }
}
