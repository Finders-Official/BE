package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.dto.response.MemberResponse;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.SocialAccountRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {
    private final MemberRepository memberRepository;
    private final MemberUserRepository memberUserRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !memberUserRepository.existsByNickname(nickname);
    }

    @Override
    public MemberResponse.MyProfile getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member realMember = (Member) Hibernate.unproxy(member);

        MemberResponse.UserDetail userDetail = null;
        if (realMember instanceof MemberUser memberUser) {
            List<MemberResponse.SocialAccountInfo> socialAccounts = socialAccountRepository.findAllByUser(member)
                    .stream()
                    .map(sa -> new MemberResponse.SocialAccountInfo(sa.getProvider().name(), sa.getSocialEmail()))
                    .toList();

            userDetail = new MemberResponse.UserDetail(
                    memberUser.getNickname(),
                    memberUser.getProfileImage(),
                    memberUser.getTokenBalance(),
                    socialAccounts
            );
        }

        return new MemberResponse.MyProfile(
                new MemberResponse.MemberInfo(
                        member.getId(),
                        member.getName(),
                        member.getPhone(),
                        member.getRole().name(),
                        member.getStatus().name()
                ),
                new MemberResponse.EditableInfo(true, true, true),
                new MemberResponse.RoleData(userDetail, null, null)
        );
    }
}
