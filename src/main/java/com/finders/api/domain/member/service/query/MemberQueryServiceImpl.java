package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.repository.MemberUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberUserRepository memberUserRepository;

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !memberUserRepository.existsByNickname(nickname);
    }
}
