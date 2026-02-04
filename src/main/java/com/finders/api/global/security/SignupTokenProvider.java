package com.finders.api.global.security;

import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.enums.SocialProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SignupTokenProvider {

    private final SecretKey secretKey;
    private final long signupTtlMs;

    public SignupTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
        this.signupTtlMs = jwtProperties.signupTokenExpiry();
    }

    public String createSignupToken(SignupTokenPayload payload) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + signupTtlMs);

        return Jwts.builder()
                .subject("signup")
                .claim("type", "SIGNUP")
                .claim("provider", payload.provider().name())
                .claim("providerId", payload.providerId())
                .claim("accessToken", payload.accessToken())
                .claim("name", payload.name())
                .claim("nickname", payload.nickname())
                .claim("profileImage", payload.profileImage())
                .claim("email", payload.email())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        SignupTokenPayload payload = parse(token);

        return new UsernamePasswordAuthenticationToken(
                payload, // Principal 자리에 payload를 넣습니다.
                token,   // Credentials
                List.of(new SimpleGrantedAuthority("ROLE_GUEST")) // Authorities
        );
    }

    // 검증 로직
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 추가 검증: tokenType이 SIGNUP인지 확인
            String type = claims.get("type", String.class);
            return "SIGNUP".equals(type);

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("AUTH_401: 잘못된 SignupToken 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("AUTH_401: 만료된 SignupToken입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("AUTH_401: 지원되지 않는 SignupToken입니다.");
        } catch (IllegalArgumentException e) {
            log.info("AUTH_401: SignupToken이 잘못되었습니다.");
        }
        return false;
    }

    public SignupTokenPayload parse(String signupToken) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(signupToken)
                .getPayload();

        String tokenType = claims.get("type", String.class);
        if (!tokenType.equals("SIGNUP")) {
            throw new IllegalArgumentException("SIGNUP 토큰이 아닙니다.");
        }

        return new SignupTokenPayload(
                SocialProvider.valueOf(claims.get("provider", String.class)),
                claims.get("providerId", String.class),
                claims.get("accessToken", String.class),
                claims.get("name", String.class),
                claims.get("nickname", String.class),
                claims.get("profileImage", String.class),
                claims.get("email", String.class)
        );
    }
}
