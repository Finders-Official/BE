package com.finders.api.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 페이지네이션 응답 구조
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@JsonPropertyOrder({"success", "code", "message", "timestamp", "data", "pagination"})
public class PagedResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final List<T> data;
    private final PaginationInfo pagination;

    @Getter
    @Builder
    public static class PaginationInfo {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;
        private final boolean hasNext;
        private final boolean hasPrevious;
    }

    /**
     * Page 객체로부터 PagedResponse 생성
     */
    public static <T> PagedResponse<T> of(SuccessCode code, Page<T> page) {
        return PagedResponse.<T>builder()
                .success(true)
                .code(code.getCode())
                .message(code.getMessage())
                .data(page.getContent())
                .pagination(PaginationInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }

    /**
     * 컨텐츠와 Page 객체로부터 PagedResponse 생성 (변환된 데이터용)
     */
    public static <T> PagedResponse<T> of(SuccessCode code, List<T> content, Page<?> page) {
        return PagedResponse.<T>builder()
                .success(true)
                .code(code.getCode())
                .message(code.getMessage())
                .data(content)
                .pagination(PaginationInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }
}
