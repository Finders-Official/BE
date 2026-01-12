package com.finders.api.global.config;

import com.finders.api.global.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final JwtTokenProvider jwtTokenProvider;
    private final SignupTokenProvider signupTokenProvider;

    // 인증 없이 접근 가능한 경로
    private static final String[] PUBLIC_ENDPOINTS = {
            // Swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            // Actuator
            "/actuator/health",
            // H2 Console (local only)
            "/h2-console/**",
            // Storage Test (local only - @Profile("local")로 프로덕션 비활성화)
            "/storage/test/**",
            // 소셜 로그인 시작점
            "/auth/social/login",
            "/auth/social/login/code",
            // 토큰 재발급
            "/auth/reissue",
            // 로그아웃
            "/auth/logout",
            // Webhooks (외부 서비스 콜백)
            "/webhooks/**",
            // TODO: Auth API 구현 후 제거 - 개발 테스트용
            "/restorations/**",
            // HM-010 커뮤니티 사진 미리 보기
            "/posts/preview"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 비활성화 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // H2 Console을 위한 frameOptions 설정
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 에러 핸들링
                        .accessDeniedHandler(jwtAccessDeniedHandler)           // 403 에러 핸들링
                )

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // GUEST, USER 모두 허용
                        .requestMatchers(
                                "/members/phone/**",
                                "/users/nickname/check"
                        ).hasAnyRole("USER", "GUEST")
                        // GUEST만 허용
                        .requestMatchers("/members/social/signup/complete").hasRole("GUEST")
                        // USER만 허용
                        .anyRequest().hasRole("USER")
                )

                // JWT 필터 추가
                 .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, signupTokenProvider),
                         UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://finders.it.kr",
                "https://www.finders.it.kr",
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
