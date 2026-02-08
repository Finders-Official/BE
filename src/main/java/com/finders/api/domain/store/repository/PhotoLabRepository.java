package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.dto.response.PhotoLabParentRegionCountResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoLabRepository extends JpaRepository<PhotoLab, Long> {
    // 커뮤니티 현상소 검색 관련 상수
    int COMMUNITY_SEARCH_LIMIT = 8;

    List<PhotoLab> findTop8ByOrderByReservationCountDescIdAsc();

    Optional<PhotoLab> findByIdAndStatus(Long id, PhotoLabStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PhotoLab p SET p.reviewCount = p.reviewCount + 1 WHERE p.id = :id")
    void incrementReviewCount(@Param("id") Long id);

    // 커뮤니티 현상소 검색
    interface PhotoLabSearchResult {
        Long getLabId();
        String getName();
        String getAddress();
        Double getDistanceVal();
    }
    @Query(value = "SELECT id AS labId, name, address, " +
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

    @Query("select new com.finders.api.domain.store.dto.response.PhotoLabParentRegionCountResponse(" +
            "cast(coalesce(pr.id, r.id) as long), " +
            "coalesce(pr.regionName, r.regionName), " +
            "count(l)) " +
            "from PhotoLab l " +
            "join l.region r " +
            "left join r.parentRegion pr " +
            "where l.status = com.finders.api.domain.store.enums.PhotoLabStatus.ACTIVE " +
            "group by cast(coalesce(pr.id, r.id) as long), coalesce(pr.regionName, r.regionName) " +
            "order by cast(coalesce(pr.id, r.id) as long)")
    List<PhotoLabParentRegionCountResponse> countPhotoLabsByTopRegion();
}
