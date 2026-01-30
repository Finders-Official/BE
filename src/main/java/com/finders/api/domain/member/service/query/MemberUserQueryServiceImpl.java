package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberUserQueryServiceImpl implements MemberUserQueryService {

    private final MemberUserRepository memberUserRepository;

    @Override
    public MemberUser getActiveMember(Long memberId) {
        MemberUser user = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (user.getStatus() != MemberStatus.ACTIVE) {
            throw new CustomException(ErrorCode.MEMBER_INACTIVE);
        }

        return user;
    }
}
