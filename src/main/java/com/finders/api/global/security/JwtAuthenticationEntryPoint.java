package com.finders.api.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("[AuthenticationEntryPoint] 401 Unauthorized: {}", request.getRequestURI());

        // 1. 상태 코드 설정 (HTTP 401)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 2. 응답 타입 설정 (JSON)
        response.setContentType("application/json;charset=UTF-8");

        // 3. ApiResponse 생성 (작성하신 error 메서드 활용)
        ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.UNAUTHORIZED);

        // 4. JSON 변환 후 출력
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}