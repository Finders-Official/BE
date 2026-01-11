package com.finders.api.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Slf4j
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
                .claim("type", "ACCESS")
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

    // 인증 & 검증 로직
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String memberId = claims.getSubject();
        String role = claims.get("role", String.class);

        if (role == null) {
            // RefreshToken 등으로 접근했을 경우 예외 처리
            throw new JwtException("권한 정보가 없는 토큰입니다.");
        }

        String authority = "ROLE_" + role;

        UserDetails principal = new User(String.valueOf(memberId), "",
                List.of(new SimpleGrantedAuthority(authority)));

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return "ACCESS".equals(claims.get("type"));
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 토큰에서 memberId 추출
    public Long getMemberIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    public long getAccessTokenExpiryMs() {
        return this.accessTtlMs;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
