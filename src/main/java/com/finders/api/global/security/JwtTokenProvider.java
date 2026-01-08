package com.finders.api.global.security;

import com.finders.api.domain.member.entity.MemberUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
        this.accessTtlMs = jwtProperties.accessTokenExpiry();
        this.refreshTtlMs = jwtProperties.refreshTokenExpiry();
    }

    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTtlMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTtlMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }
}
