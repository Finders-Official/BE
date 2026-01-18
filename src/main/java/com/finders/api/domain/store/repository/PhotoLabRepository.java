package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoLabRepository extends JpaRepository<PhotoLab, Long> {
    // 커뮤니티 현상소 검색 관련 상수
    int COMMUNITY_SEARCH_LIMIT = 8;

    List<PhotoLab> findTop8ByOrderByReservationCountDescIdAsc();

    Optional<PhotoLab> findByIdAndStatus(Long id, PhotoLabStatus status);

    // 커뮤니티 현상소 검색
    interface PhotoLabSearchResult {
        Long getId();
        String getName();
        String getAddress();
        Double getDistanceVal();
    }
    @Query(value = "SELECT id, name, address, " +
            // 직선 거리 계산
            "IF(:locationAgreed = true, ST_Distance_Sphere(point(:lng, :lat), point(longitude, latitude)), NULL) AS distance_val " +
            "FROM photo_lab " +
            "WHERE status = 'ACTIVE' AND name LIKE CONCAT('%', :keyword, '%') " +

            "ORDER BY " +
            // 1순위 정확도
            "CASE WHEN name LIKE CONCAT(:keyword, '%') THEN 0 ELSE 1 END ASC, " +
            // 2순위 거리
            "CASE WHEN :locationAgreed = true THEN distance_val END ASC, " +
            // 3순위 예약 수
            "reservation_count DESC " +

            "LIMIT " + COMMUNITY_SEARCH_LIMIT,
            nativeQuery = true)
    List<PhotoLabSearchResult> searchCommunityPhotoLabs(
            @Param("keyword") String keyword,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("locationAgreed") boolean locationAgreed
    );
}
