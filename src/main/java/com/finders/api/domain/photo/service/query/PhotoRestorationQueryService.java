package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.photo.dto.response.RestorationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 사진 복원 Query 서비스 인터페이스
 * <p>
 * 복원 결과 조회, 이력 조회를 담당합니다.
 */
public interface PhotoRestorationQueryService {

    /**
     * 복원 결과 조회
     *
     * @param memberId      회원 ID
     * @param restorationId 복원 ID
     * @return 복원 상세 정보
     */
    RestorationResponse.Detail getRestoration(Long memberId, Long restorationId);

    /**
     * 복원 이력 조회 (페이지네이션)
     *
     * @param memberId 회원 ID
     * @param pageable 페이지 정보
     * @return 복원 이력 페이지
     */
    Page<RestorationResponse.Summary> getRestorationHistory(Long memberId, Pageable pageable);
}
