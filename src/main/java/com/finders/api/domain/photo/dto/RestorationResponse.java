package com.finders.api.domain.photo.dto;

import com.finders.api.domain.photo.entity.PhotoRestoration;
import com.finders.api.domain.photo.enums.FeedbackRating;
import com.finders.api.domain.photo.enums.RestorationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 복원 응답 DTO
 */
public class RestorationResponse {

    /**
     * 복원 생성 응답
     */
    @Builder
    public record Created(
            Long id,
            RestorationStatus status,
            int tokenUsed,
            int remainingBalance
    ) {}

    /**
     * 복원 상세 응답
     */
    @Builder
    public record Detail(
            Long id,
            String originalUrl,
            String restoredUrl,
            RestorationStatus status,
            int tokenUsed,
            FeedbackRating feedbackRating,
            String feedbackComment,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        public static Detail from(PhotoRestoration restoration, String originalSignedUrl, String restoredSignedUrl) {
            return Detail.builder()
                    .id(restoration.getId())
                    .originalUrl(originalSignedUrl)
                    .restoredUrl(restoredSignedUrl)
                    .status(restoration.getStatus())
                    .tokenUsed(restoration.getTokenUsed())
                    .feedbackRating(restoration.getFeedbackRating())
                    .feedbackComment(restoration.getFeedbackComment())
                    .errorMessage(restoration.getErrorMessage())
                    .createdAt(restoration.getCreatedAt())
                    .build();
        }
    }

    /**
     * 복원 목록 요약 응답
     */
    @Builder
    public record Summary(
            Long id,
            String thumbnailUrl,
            RestorationStatus status,
            LocalDateTime createdAt
    ) {
        public static Summary from(PhotoRestoration restoration, String thumbnailSignedUrl) {
            return Summary.builder()
                    .id(restoration.getId())
                    .thumbnailUrl(thumbnailSignedUrl)
                    .status(restoration.getStatus())
                    .createdAt(restoration.getCreatedAt())
                    .build();
        }
    }
}
