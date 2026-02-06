package com.finders.api.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 응답 코드
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseCode {

    // ========================================
    // Common (4xx, 5xx)
    // ========================================
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "허용되지 않은 HTTP 메서드입니다."),
    CONFLICT(HttpStatus.CONFLICT, "COMMON_409", "리소스 충돌이 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

    // ========================================
    // Validation
    // ========================================
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VALID_400", "입력값이 올바르지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "VALID_401", "필수 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "VALID_402", "파라미터 타입이 올바르지 않습니다."),

    // ========================================
    // Auth
    // ========================================
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_402", "만료된 토큰입니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_403", "인증 정보가 올바르지 않습니다."),
    AUTH_OAUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH_400", "소셜 로그인에 실패했습니다."),

    // ========================================
    // Member
    // ========================================
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409", "이미 존재하는 회원입니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_410", "이미 사용 중인 닉네임입니다."),

    // ========================================
    // Store (현상소)
    // ========================================
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_404", "현상소를 찾을 수 없습니다."),

    // ========================================
    // Reservation (예약)
    // ========================================
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_404", "예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION_409", "해당 시간에 이미 예약이 있습니다."),
    RESERVATION_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "RESERVATION_400", "예약을 취소할 수 없습니다."),

    // ========================================
    // Photo
    // ========================================
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTO_404", "사진을 찾을 수 없습니다."),
    PHOTO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_500", "사진 업로드에 실패했습니다."),
    PHOTO_RESTORE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_501", "사진 복구에 실패했습니다."),

    // ========================================
    // Photo Restoration
    // ========================================
    RESTORATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESTORATION_404", "복원 요청을 찾을 수 없습니다."),
    INVALID_RESTORATION_STATE(HttpStatus.BAD_REQUEST, "RESTORATION_400", "유효하지 않은 복원 상태입니다."),
    RESTORATION_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RESTORATION_500", "복원 처리 중 오류가 발생했습니다."),
    INSUFFICIENT_TOKENS(HttpStatus.PAYMENT_REQUIRED, "TOKEN_402", "토큰이 부족합니다."),

    // ========================================
    // External API
    // ========================================
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_503", "외부 API 호출에 실패했습니다."),
    VISION_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "VISION_503", "Vision AI 호출에 실패했습니다."),

    // ========================================
    // Replicate API
    // ========================================
    REPLICATE_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "REPLICATE_503", "Replicate API 호출에 실패했습니다."),
    REPLICATE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "REPLICATE_504", "Replicate API 응답 시간이 초과되었습니다."),

    // ========================================
    // GCS (Google Cloud Storage)
    // ========================================
    GCS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GCS_500", "이미지 업로드에 실패했습니다."),
    GCS_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GCS_501", "이미지 다운로드에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
