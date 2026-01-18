package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.PhotoRestoration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PhotoRestoration 리포지토리
 */
public interface PhotoRestorationRepository extends JpaRepository<PhotoRestoration, Long> {

    /**
     * 회원 ID로 복원 이력 조회 (페이지네이션)
     * <p>
     * 정렬은 Pageable로 제어합니다. (기본: createdAt DESC)
     */
    Page<PhotoRestoration> findByMemberId(Long memberId, Pageable pageable);

    /**
     * Replicate Prediction ID로 복원 조회
     */
    Optional<PhotoRestoration> findByReplicatePredictionId(String predictionId);
}
