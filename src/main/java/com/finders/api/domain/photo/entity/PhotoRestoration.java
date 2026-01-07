package com.finders.api.domain.photo.entity;

import com.finders.api.domain.photo.enums.FeedbackRating;
import com.finders.api.domain.photo.enums.RestorationStatus;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 사진 복원 요청
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "photo_restoration",
        indexes = @Index(name = "idx_restoration_member", columnList = "member_id, status")
)
public class PhotoRestoration extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "original_url", length = 500, nullable = false)
    private String originalUrl;

    @Column(name = "mask_url", length = 500, nullable = false)
    private String maskUrl;

    @Column(name = "restored_url", length = 500)
    private String restoredUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RestorationStatus status = RestorationStatus.PENDING;

    @Column(name = "replicate_prediction_id", length = 100)
    private String replicatePredictionId;

    @Column(name = "token_used", nullable = false)
    private int tokenUsed = 1;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_rating", length = 10)
    private FeedbackRating feedbackRating;

    @Column(name = "feedback_comment", length = 500)
    private String feedbackComment;

    @Builder
    private PhotoRestoration(Long memberId, String originalUrl, String maskUrl, int tokenUsed) {
        this.memberId = memberId;
        this.originalUrl = originalUrl;
        this.maskUrl = maskUrl;
        this.tokenUsed = tokenUsed;
        this.status = RestorationStatus.PENDING;
    }

    public void startProcessing(String predictionId) {
        this.status = RestorationStatus.PROCESSING;
        this.replicatePredictionId = predictionId;
    }

    public void complete(String restoredUrl) {
        this.status = RestorationStatus.COMPLETED;
        this.restoredUrl = restoredUrl;
    }

    public void fail(String errorMessage) {
        this.status = RestorationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void addFeedback(FeedbackRating rating, String comment) {
        this.feedbackRating = rating;
        this.feedbackComment = comment;
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isCompleted() {
        return this.status == RestorationStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == RestorationStatus.FAILED;
    }
}
