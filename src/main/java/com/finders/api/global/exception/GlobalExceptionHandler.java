package com.finders.api.global.exception;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.discord.DiscordWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordWebhookService discordWebhookService;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(
            CustomException e,
            HttpServletRequest request
    ) {
        log.warn("[CustomException] {} - {}", e.getErrorCode().getCode(), e.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("[ValidationException] {} - {}", request.getRequestURI(), errors);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            errors.put(field, violation.getMessage());
        }

        log.warn("[ConstraintViolation] {} - {}", request.getRequestURI(), errors);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT, errors));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request
    ) {
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        log.warn("[MissingParameter] {} - {}", request.getRequestURI(), message);

        return ResponseEntity
                .status(ErrorCode.MISSING_PARAMETER.getStatus())
                .body(ApiResponse.error(ErrorCode.MISSING_PARAMETER, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다.", e.getName());
        log.warn("[TypeMismatch] {} - {}", request.getRequestURI(), message);

        return ResponseEntity
                .status(ErrorCode.TYPE_MISMATCH.getStatus())
                .body(ApiResponse.error(ErrorCode.TYPE_MISMATCH, message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        log.warn("[MethodNotAllowed] {} - {}", request.getRequestURI(), e.getMethod());

        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request
    ) {
        log.warn("[NotFound] {}", request.getRequestURI());

        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.error(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler({RedisConnectionFailureException.class, QueryTimeoutException.class})
    public ResponseEntity<ApiResponse<Void>> handleRedisException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("[UnhandledException] {} - {}", request.getRequestURI(), e.getMessage(), e);

        try {
            discordWebhookService.sendErrorNotification(e, request.getMethod(), request.getRequestURI());
        } catch (Exception ignored) {
            log.warn("[DiscordWebhook] Failed to send notification", ignored);
        }

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
