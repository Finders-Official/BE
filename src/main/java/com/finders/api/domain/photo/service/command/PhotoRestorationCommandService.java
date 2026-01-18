package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;

/**
 * 사진 복원 Command 서비스 인터페이스
 * <p>
 * 복원 요청 생성, 피드백 등록, Webhook 처리를 담당합니다.
 */
public interface PhotoRestorationCommandService {

    /**
     * 복원 요청 생성
     *
     * @param memberId 회원 ID
     * @param request  복원 요청 (originalPath, maskPath)
     * @return 생성된 복원 정보
     */
    RestorationResponse.Created createRestoration(Long memberId, RestorationRequest.Create request);

    /**
     * 복원 결과 피드백 등록
     *
     * @param memberId      회원 ID
     * @param restorationId 복원 ID
     * @param request       피드백 요청 (rating, comment)
     */
    void addFeedback(Long memberId, Long restorationId, RestorationRequest.Feedback request);

    /**
     * 복원 완료 처리 (Webhook)
     *
     * @param predictionId      Replicate Prediction ID
     * @param restoredImageUrl  복원된 이미지 URL
     */
    void completeRestoration(String predictionId, String restoredImageUrl);

    /**
     * 복원 실패 처리 (Webhook)
     *
     * @param predictionId  Replicate Prediction ID
     * @param errorMessage  에러 메시지
     */
    void failRestoration(String predictionId, String errorMessage);
}
