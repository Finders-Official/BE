package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.PhotoRestoration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * PhotoRestoration 리포지토리
 */
public interface PhotoRestorationRepository extends JpaRepository<PhotoRestoration, Long> {

    List<PhotoRestoration> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    Page<PhotoRestoration> findByMemberId(Long memberId, Pageable pageable);

    Optional<PhotoRestoration> findByReplicatePredictionId(String predictionId);
}
