package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoLabImageRepository extends JpaRepository<PhotoLabImage, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PhotoLabImage image set image.isMain = false " +
            "where image.photoLab.id = :photoLabId and image.isMain = true")
    int clearMainByPhotoLabId(@Param("photoLabId") Long photoLabId);
}
