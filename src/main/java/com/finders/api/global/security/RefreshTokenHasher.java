package com.finders.api.global.security;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenHasher {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public void saveRefreshToken(Long memberId, String rawRefreshToken) {
        String hashedToken = encoder.encode(rawRefreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateRefreshTokenHash(hashedToken);
    }

    public boolean matches(String rawRefreshToken, String hash) {
        if (hash == null) return false;
        return encoder.matches(rawRefreshToken, hash);
    }
}
