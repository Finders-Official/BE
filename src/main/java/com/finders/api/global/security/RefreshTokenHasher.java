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
        // JWT 원본을 SHA-256으로 해싱
        String preHashedToken = hashToken(rawRefreshToken);
        // 해싱된 값 암호화
        String bcryptHashedToken = encoder.encode(preHashedToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateRefreshTokenHash(bcryptHashedToken);
    }

    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    public boolean matches(String rawRefreshToken, String encodedHash) {
        if (encodedHash == null) return false;

        // 입력받은 원본 토큰을 SHA-256으로 압축
        String preHashed = hashToken(rawRefreshToken);

        // 압축된 값을 DB의 BCrypt 해시와 비교
        return encoder.matches(preHashed, encodedHash);
    }
}
