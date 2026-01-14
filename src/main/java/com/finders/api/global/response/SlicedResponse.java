package com.finders.api.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonPropertyOrder({"success", "code", "message", "timestamp", "data", "slice"})
public class SlicedResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final List<T> data;
    private final SliceInfo slice;

    @Getter
    @Builder
    public static class SliceInfo {
        private final int page;
        private final int size;
        private final boolean first;
        private final boolean hasNext;
        private final boolean hasPrevious;
    }

    public static <T> SlicedResponse<T> of(SuccessCode code, Slice<T> slice) {
        return SlicedResponse.<T>builder()
                .success(true)
                .code(code.getCode())
                .message(code.getMessage())
                .data(slice.getContent())
                .slice(SliceInfo.builder()
                        .page(slice.getNumber())
                        .size(slice.getSize())
                        .first(slice.isFirst())
                        .hasNext(slice.hasNext())
                        .hasPrevious(slice.hasPrevious())
                        .build())
                .build();
    }

    public static <T> SlicedResponse<T> of(SuccessCode code, List<T> content, Slice<?> slice) {
        return SlicedResponse.<T>builder()
                .success(true)
                .code(code.getCode())
                .message(code.getMessage())
                .data(content)
                .slice(SliceInfo.builder()
                        .page(slice.getNumber())
                        .size(slice.getSize())
                        .first(slice.isFirst())
                        .hasNext(slice.hasNext())
                        .hasPrevious(slice.hasPrevious())
                        .build())
                .build();
    }
}
