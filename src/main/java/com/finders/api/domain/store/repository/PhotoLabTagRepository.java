package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoLabTagRepository extends JpaRepository<PhotoLabTag, Long> {

    @Query("select k from PhotoLabTag k where k.photoLab.id in :photoLabIds")
    List<PhotoLabTag> findByPhotoLabIds(@Param("photoLabIds") List<Long> photoLabIds);
}
