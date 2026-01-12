package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoLabImageRepository extends JpaRepository<PhotoLabImage, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PhotoLabImage image set image.isMain = false " +
            "where image.photoLab.id = :photoLabId and image.isMain = true")
    int clearMainByPhotoLabId(@Param("photoLabId") Long photoLabId);

    @Query("select coalesce(max(image.displayOrder), -1) from PhotoLabImage image where image.photoLab.id = :photoLabId")
    Integer findMaxDisplayOrderByPhotoLabId(@Param("photoLabId") Long photoLabId);

    @Query("""
            select image
            from PhotoLabImage image
            where image.photoLab.id in :photoLabIds
              and image.isMain = true
            order by image.photoLab.id asc, image.displayOrder asc, image.id asc
            """)
    List<PhotoLabImage> findMainImagesByPhotoLabIds(@Param("photoLabIds") List<Long> photoLabIds);

    @Query("""
            select image
            from PhotoLabImage image
            where image.photoLab.id in :photoLabIds
            order by image.photoLab.id asc, image.displayOrder asc, image.id asc
            """)
    List<PhotoLabImage> findByPhotoLabIds(@Param("photoLabIds") List<Long> photoLabIds);
}

