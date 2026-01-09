package com.finders.api.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 성공 응답 코드
 */
@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseCode {

    // ========================================
    // Common (2xx)
    // ========================================
    OK(HttpStatus.OK, "COMMON_200", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COMMON_201", "리소스가 성공적으로 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON_204", "요청이 성공적으로 처리되었습니다."),

    // ========================================
    // Auth
    // ========================================
    AUTH_LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200", "로그인에 성공했습니다."),
    AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_201", "로그아웃에 성공했습니다."),
    AUTH_TOKEN_REFRESHED(HttpStatus.OK, "AUTH_202", "토큰이 갱신되었습니다."),

    // ========================================
    // Member
    // ========================================
    MEMBER_CREATED(HttpStatus.CREATED, "MEMBER_201", "회원가입이 완료되었습니다."),
    MEMBER_FOUND(HttpStatus.OK, "MEMBER_200", "회원 조회에 성공했습니다."),
    MEMBER_UPDATED(HttpStatus.OK, "MEMBER_201", "회원 정보가 수정되었습니다."),
    MEMBER_DELETED(HttpStatus.OK, "MEMBER_202", "회원 탈퇴가 완료되었습니다."),
    MEMBER_ME_FOUND(HttpStatus.OK, "MEMBER_203", "내 정보 조회에 성공했습니다."),

    // ========================================
    // Store (현상소)
    // ========================================
    STORE_FOUND(HttpStatus.OK, "STORE_200", "현상소 조회에 성공했습니다."),
    STORE_LIST_FOUND(HttpStatus.OK, "STORE_201", "현상소 목록 조회에 성공했습니다."),

    // ========================================
    // Reservation (예약)
    // ========================================
    RESERVATION_CREATED(HttpStatus.CREATED, "RESERVATION_201", "예약이 완료되었습니다."),
    RESERVATION_FOUND(HttpStatus.OK, "RESERVATION_200", "예약 조회에 성공했습니다."),
    RESERVATION_CANCELLED(HttpStatus.OK, "RESERVATION_202", "예약이 취소되었습니다."),

    // ========================================
    // Photo
    // ========================================
    PHOTO_UPLOADED(HttpStatus.CREATED, "PHOTO_201", "사진이 업로드되었습니다."),
    PHOTO_RESTORED(HttpStatus.OK, "PHOTO_200", "사진 복구가 완료되었습니다."),

    // ========================================
    // Restoration (AI 복원)
    // ========================================
    RESTORATION_CREATED(HttpStatus.ACCEPTED, "RESTORATION_202", "복원 요청이 생성되었습니다."),
    RESTORATION_FOUND(HttpStatus.OK, "RESTORATION_200", "복원 결과 조회에 성공했습니다."),

    // ========================================
    // Token
    // ========================================
    TOKEN_BALANCE_FOUND(HttpStatus.OK, "TOKEN_200", "토큰 잔액 조회에 성공했습니다."),
    TOKEN_HISTORY_FOUND(HttpStatus.OK, "TOKEN_201", "토큰 이력 조회에 성공했습니다."),

    // ========================================
    // Community
    // ========================================
    POST_CREATED(HttpStatus.CREATED, "POST_201", "게시글이 등록되었습니다."),
    POST_FOUND(HttpStatus.OK, "POST_200", "게시글 조회에 성공했습니다."),

    // ========================================
    // Storage
    // ========================================
    STORAGE_UPLOADED(HttpStatus.CREATED, "STORAGE_201", "파일이 업로드되었습니다."),
    STORAGE_DELETED(HttpStatus.OK, "STORAGE_200", "파일이 삭제되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
