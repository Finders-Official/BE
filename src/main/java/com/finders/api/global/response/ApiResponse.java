package com.finders.api.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 구조
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@JsonPropertyOrder({"success", "code", "message", "timestamp", "data"})
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    // ========================================
    // 성공 응답 생성 메서드
    // ========================================

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(SuccessCode code, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success(SuccessCode code) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code.getCode())
                .message(code.getMessage())
                .build();
    }

    /**
     * OK 응답 (단축 메서드)
     */
    public static <T> ApiResponse<T> ok(T data) {
        return success(SuccessCode.OK, data);
    }

    /**
     * Created 응답 (단축 메서드)
     */
    public static <T> ApiResponse<T> created(T data) {
        return success(SuccessCode.CREATED, data);
    }

    // ========================================
    // 에러 응답 생성 메서드
    // ========================================

    /**
     * 에러 응답
     */
    public static <T> ApiResponse<T> error(ErrorCode code) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code.getCode())
                .message(code.getMessage())
                .build();
    }

    /**
     * 에러 응답 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> error(ErrorCode code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code.getCode())
                .message(message)
                .build();
    }

    /**
     * 에러 응답 (에러 데이터 포함)
     */
    public static <T> ApiResponse<T> error(ErrorCode code, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();
    }
}
