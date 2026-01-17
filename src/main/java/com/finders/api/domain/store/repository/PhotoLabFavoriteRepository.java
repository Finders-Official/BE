package com.finders.api.domain.store.repository;

import com.finders.api.domain.member.entity.FavoritePhotoLab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhotoLabFavoriteRepository extends JpaRepository<FavoritePhotoLab, Long> {

    @Query("select f.photoLab.id from FavoritePhotoLab f where f.member.id = :memberId and f.photoLab.id in :photoLabIds")
    List<Long> findFavoritePhotoLabIds(@Param("memberId") Long memberId, @Param("photoLabIds") List<Long> photoLabIds);

    boolean existsByMember_IdAndPhotoLab_Id(Long memberId, Long photoLabId);

    Optional<FavoritePhotoLab> findByMember_IdAndPhotoLab_Id(Long memberId, Long photoLabId);
}
