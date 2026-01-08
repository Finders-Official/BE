package com.finders.api.global.security;

import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.enums.SocialProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

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
                .claim("tokenType", "SIGNUP")
                .claim("provider", payload.provider().name())
                .claim("providerId", payload.providerId())
                .claim("name", payload.name())
                .claim("nickname", payload.nickname())
                .claim("profileImage", payload.profileImage())
                .claim("email", payload.email())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public SignupTokenPayload parse(String signupToken) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(signupToken)
                .getPayload();

        String tokenType = claims.get("tokenType", String.class);
        if (!tokenType.equals("SIGNUP")) {
            throw new IllegalArgumentException("SIGNUP 토큰이 아닙니다.");
        }

        SocialProvider provider = SocialProvider.valueOf(claims.get("provider", String.class));
        String providerId = claims.get("providerId", String.class);

        return new SignupTokenPayload(
                SocialProvider.valueOf(claims.get("provider", String.class)),
                claims.get("providerId", String.class),
                claims.get("name", String.class),
                claims.get("nickname", String.class),
                claims.get("profileImage", String.class),
                claims.get("email", String.class)
        );
    }
}
