package com.finders.api.global.security;

import java.time.Duration;
import java.util.Optional;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.redis.RedisCacheClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenHasher {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;
    private final RedisCacheClient redisCacheClient;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final String RT_KEY_PREFIX = "rt:";

    @Transactional
    public void saveRefreshToken(Long memberId, String rawRefreshToken) {
        // JWT 원본을 SHA-256으로 해싱
        String preHashedToken = hashToken(rawRefreshToken);
        // 해싱된 값 암호화
        String bcryptHashedToken = encoder.encode(preHashedToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateRefreshTokenHash(bcryptHashedToken);

        try {
            long ttlSeconds = jwtProperties.refreshTokenExpiry() / 1000;

            redisTemplate.opsForValue().set(
                RT_KEY_PREFIX + memberId,
                preHashedToken,
                Duration.ofSeconds(ttlSeconds)
            );
        } catch (Exception e) {
            log.error("[RefreshTokenHasher.saveRefreshToken] RefreshToken 캐싱 실패: {}", e.getMessage());
        }
    }

    public boolean matches(Long memberId, String rawRefreshToken, String encodedHash) {
        String inputHashed = hashToken(rawRefreshToken);
        Optional<Object> savedToken = redisCacheClient.get(RT_KEY_PREFIX + memberId);

        if (savedToken.isPresent()) {
            return savedToken.get().equals(inputHashed);
        }

        if (encodedHash == null) return false;
        String preHashed = hashToken(rawRefreshToken);
        return encoder.matches(preHashed, encodedHash);
    }

    public void removeRefreshToken(Long memberId) {
        try {
            redisTemplate.delete(RT_KEY_PREFIX + memberId);
        } catch (Exception e) {
            log.error("[RefreshTokenHasher.removeRefreshToken] RefreshToken 삭제 실패: {}", e.getMessage());
        }
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
}
