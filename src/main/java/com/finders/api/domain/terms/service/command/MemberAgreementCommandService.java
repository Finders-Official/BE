package com.finders.api.domain.terms.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.SocialProvider;

import java.util.List;

public interface MemberAgreementCommandService {

    void saveAgreementsFromSocial(MemberUser user, SocialProvider provider, List<String> socialTags);
}
