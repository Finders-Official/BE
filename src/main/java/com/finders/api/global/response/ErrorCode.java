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

    // 토큰 관련
    AUTH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증 토큰이 존재하지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_402", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_403", "만료된 토큰입니다."),

    // Provider 요청/검증 (kakao, apple 등)
    AUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_410", "지원하지 않는 소셜 로그인 제공자입니다."),
    AUTH_INVALID_PROVIDER_REQUEST(HttpStatus.BAD_REQUEST, "AUTH_411", "소셜 로그인 요청 정보가 올바르지 않습니다."),
    AUTH_INVALID_ROLE(HttpStatus.FORBIDDEN, "AUTH_430", "해당 서비스에 접근 권한이 없는 계정 타입입니다."),

    // OAuth 로그인
    AUTH_OAUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH_400", "소셜 로그인에 실패했습니다."),
    AUTH_OAUTH_TOKEN_FAILED(HttpStatus.BAD_REQUEST, "AUTH_404", "소셜 토큰 발급에 실패했습니다."),
    AUTH_OAUTH_PROFILE_FAILED(HttpStatus.BAD_REQUEST, "AUTH_405", "소셜 사용자 정보를 불러오지 못했습니다."),

    // 계정/연동 정책
    AUTH_SOCIAL_ACCOUNT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_406", "가입되지 않은 소셜 계정입니다."),
    AUTH_PROVIDER_MISMATCH(HttpStatus.CONFLICT, "AUTH_409", "다른 소셜 계정으로 가입된 회원입니다."),
    AUTH_SOCIAL_ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "AUTH_412", "이용이 제한된 계정입니다."),

    // 약관/추가정보
    AUTH_TERMS_NOT_AGREED(HttpStatus.FORBIDDEN, "AUTH_413", "필수 약관에 동의하지 않았습니다."),
    AUTH_ADDITIONAL_INFO_REQUIRED(HttpStatus.FORBIDDEN, "AUTH_414", "추가 정보 입력이 필요합니다."),

    // 휴대폰 인증
    AUTH_PHONE_TOO_MANY_REQUESTS(HttpStatus.BAD_REQUEST, "AUTH_415", "인증번호 요청이 너무 많습니다."),
    AUTH_PHONE_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_420", "인증번호가 올바르지 않습니다."),
    AUTH_PHONE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_421", "인증번호가 만료되었거나 존재하지 않습니다."),
    AUTH_PHONE_MAX_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_422", "인증 시도 횟수를 초과했습니다."),
    AUTH_PHONE_ALREADY_VERIFIED(HttpStatus.CONFLICT, "AUTH_423", "이미 인증이 완료된 요청입니다."),

    // 로그인 실패
    AUTH_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "AUTH_401", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // ========================================
    // Member
    // ========================================
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409", "이미 존재하는 회원입니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_410", "이미 사용 중인 닉네임입니다."),
    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_411", "이미 사용 중인 이메일입니다."),
    MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "MEMBER_402", "비활성화되었거나 이미 탈퇴한 계정입니다."),
    MEMBER_WITHDRAWAL_LOCKED(HttpStatus.FORBIDDEN, "MEMBER_400", "진행 중인 서비스가 있어 탈퇴가 불가능합니다."),

    // 휴대폰 인증 증빙 관련 (VPT 검증)
    MEMBER_PHONE_VERIFY_REQUIRED(HttpStatus.BAD_REQUEST, "MEMBER_420", "휴대폰 인증이 필요합니다."),
    MEMBER_PHONE_VERIFY_FAILED(HttpStatus.BAD_REQUEST, "MEMBER_421", "휴대폰 인증에 실패했습니다."),

    // 약관 관련
    MEMBER_MANDATORY_TERMS_NOT_AGREED(HttpStatus.FORBIDDEN, "MEMBER_430", "필수 약관에 동의하지 않았습니다."),

    // ========================================
    // Store (현상소)
    // ========================================
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_404", "현상소를 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "STORE_403", "해당 현상소에 접근 권한이 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION_404", "지역을 찾을 수 없습니다."),
    BUSINESS_HOUR_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSINESS_HOUR_404", "현상소의 영업시간을 찾을 수 없습니다."),

    // ========================================
    // Reservation (예약)
    // ========================================
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_404", "예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION_409", "해당 시간에 이미 예약이 있습니다."),
    RESERVATION_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "RESERVATION_400", "예약을 취소할 수 없습니다."),
    RESERVATION_FULL(HttpStatus.CONFLICT, "RESERVATION_409_FULL", "해당 시간대의 예약이 모두 마감되었습니다."),
    RESERVATION_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_SLOT_404", "예약 슬롯을 찾을 수 없습니다."),

    // ========================================
    // Photo
    // ========================================
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTO_404", "사진을 찾을 수 없습니다."),
    PHOTO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_500", "사진 업로드에 실패했습니다."),
    PHOTO_RESTORE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_501", "사진 복구에 실패했습니다."),
    PHOTO_FILES_REQUIRED(HttpStatus.BAD_REQUEST, "PHOTO_400_FILES_REQUIRED", "스캔 이미지 파일은 필수입니다."),
    PHOTO_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "PHOTO_403_OWNER_MISMATCH", "해당 현상소의 오너가 아닙니다."),
    PHOTO_RESERVATION_MISMATCH(HttpStatus.BAD_REQUEST, "PHOTO_400_RESERVATION_MISMATCH", "해당 현상소의 예약이 아닙니다."),
    PHOTO_ORDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PHOTO_409_ORDER_EXISTS", "이미 해당 예약으로 현상 주문이 생성되었습니다."),
    PHOTO_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "PHOTO_400_MEMBER_REQUIRED", "현장 접수 시 회원 정보가 필요합니다."),
    PHOTO_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTO_404_ORDER_NOT_FOUND", "현상 주문을 찾을 수 없습니다."),
    PHOTO_ORDER_PHOTOLAB_MISMATCH(HttpStatus.BAD_REQUEST, "PHOTO_400_ORDER_PHOTOLAB_MISMATCH", "해당 현상소의 주문이 아닙니다."),
    PHOTO_PHOTOLAB_ACCOUNT_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "PHOTO_400_PHOTOLAB_ACCOUNT_NOT_REGISTERED", "현상소의 사업자 계좌 정보가 등록되어 있지 않습니다."),
    PHOTO_PHOTOLAB_ACCOUNT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PHOTO_403_PHOTOLAB_ACCOUNT_ACCESS_DENIED", "해당 주문에 대한 현상소 계좌 정보에 접근할 수 없습니다."),
    PHOTO_PRINT_ORDER_NOT_FOUND(HttpStatus.FORBIDDEN, "PHOTO_404_PRINT_ORDER_NOT_FOUND", "해당 인화 주문을 찾을 수 없습니다."),
    PHOTO_PRINT_ORDER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "PHOTO_400_PRINT_ORDER_STATUS_INVALID", "현재 인화 주문 상태에서는 해당 작업을 수행할 수 없습니다."),
    PHOTO_PRINT_ORDER_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "PHOTO_403_PRINT_ORDER_OWNER_MISMATCH", "해당 인화 주문을 처리할 권한이 없습니다."),
    PHOTO_PAYMENT_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "PHOTO_409_PAYMENT_ALREADY_SUBMITTED", "이미 입금 증빙이 제출된 주문입니다."),
    PHOTO_DELIVERY_ALREADY_CREATED(HttpStatus.CONFLICT, "PHOTO_409_DELIVERY_ALREADY_CREATED", "이미 배송 정보가 등록된 주문입니다."),
    PHOTO_DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTO_404_DELIVERY_NOT_FOUND", "배송 정보를 찾을 수 없습니다."),
    PHOTO_DELIVERY_STATUS_INVALID(HttpStatus.BAD_REQUEST, "PHOTO_400_DELIVERY_STATUS_INVALID", "현재 배송 상태에서는 해당 작업을 수행할 수 없습니다."),
    PHOTO_PRINT_STATUS_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PHOTO_400_PRINT_ORDER_INVALID", "현재 인화 주문 상태에서는 해당 작업을 수행할 수 없습니다."),


    // ========================================
    // Token
    // ========================================
    INSUFFICIENT_TOKEN(HttpStatus.PAYMENT_REQUIRED, "TOKEN_402", "토큰이 부족합니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN_404", "토큰 정보를 찾을 수 없습니다."),

    // ========================================
    // Payment (결제)
    // ========================================
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PAYMENT_409", "이미 존재하는 결제 ID입니다."),
    PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAYMENT_403", "해당 결제에 접근 권한이 없습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT_410", "이미 처리된 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_400", "결제 금액이 일치하지 않습니다."),
    PAYMENT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT_401", "처리할 수 없는 결제 상태입니다."),
    PAYMENT_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PAYMENT_402", "취소할 수 없는 결제 상태입니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500", "결제 취소에 실패했습니다."),
    PAYMENT_CANCEL_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "PAYMENT_412", "취소 금액이 결제 금액을 초과할 수 없습니다."),
    INVALID_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT_403", "잘못된 결제 요청입니다."),
    WEBHOOK_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_411", "웹훅 검증에 실패했습니다."),

    // ========================================
    // External API
    // ========================================
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_503", "외부 API 호출에 실패했습니다."),
    KAKAO_UNLINK_FAILED(HttpStatus.BAD_REQUEST, "KAKAO_500", "카카오 연결 끊기 중 오류가 발생했습니다."),

    // ========================================
    // Storage (GCS)
    // ========================================
    STORAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_500", "파일 업로드에 실패했습니다."),
    STORAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_501", "파일 삭제에 실패했습니다."),
    STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORAGE_404", "파일을 찾을 수 없습니다."),
    STORAGE_INVALID_PATH(HttpStatus.BAD_REQUEST, "STORAGE_400", "잘못된 저장 경로입니다."),
    STORAGE_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "STORAGE_401", "허용되지 않는 파일 형식입니다."),
    STORAGE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "STORAGE_402", "파일 크기가 제한을 초과했습니다."),
    STORAGE_SIGNED_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_502", "Signed URL 생성에 실패했습니다."),
    STORAGE_INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "STORAGE_403", "해당 API에서 지원하지 않는 업로드 카테고리입니다."),
    STORAGE_UNAUTHORIZED(HttpStatus.FORBIDDEN, "STORAGE_405", "해당 경로에 대한 업로드 권한이 없습니다."),

    // ========================================
    // Inquiry (문의)
    // ========================================
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY_404", "문의를 찾을 수 없습니다."),
    INQUIRY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "INQUIRY_403", "해당 문의에 접근 권한이 없습니다."),
    INQUIRY_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "INQUIRY_400", "이미 종료된 문의입니다."),

    // ========================================
    // Community (사진 수다)
    // ========================================
    REVIEW_TOO_SHORT(HttpStatus.BAD_REQUEST, "COMMUNITY_401", "리뷰는 최소 20자 이상 작성해야 합니다."),
    REVIEW_TOO_LONG(HttpStatus.BAD_REQUEST, "COMMUNITY_402", "리뷰는 최대 300자 이내로 작성해야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
