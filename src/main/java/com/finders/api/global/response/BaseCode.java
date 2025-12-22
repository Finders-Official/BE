package com.finders.api.global.response;

import org.springframework.http.HttpStatus;

/**
 * 응답 코드 인터페이스
 */
public interface BaseCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
