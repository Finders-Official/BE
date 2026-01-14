package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoLabKeywordRepository extends JpaRepository<PhotoLabKeyword, Long> {

    @Query("select k from PhotoLabKeyword k where k.photoLab.id in :photoLabIds")
    List<PhotoLabKeyword> findByPhotoLabIds(@Param("photoLabIds") List<Long> photoLabIds);
}
