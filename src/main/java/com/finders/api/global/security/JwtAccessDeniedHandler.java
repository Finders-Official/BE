package com.finders.api.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.warn("[JwtAccessDeniedHandler.handle] 403 Forbidden: {}, error: {}",
                request.getRequestURI(), accessDeniedException.getMessage());

        // 1. 상태 코드 설정 (HTTP 403)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 2. 응답 타입 설정 (JSON)
        response.setContentType("application/json;charset=UTF-8");

        // 3. ApiResponse 생성
        ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.FORBIDDEN);

        // 4. JSON 변환 후 출력
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}