package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoLabKeywordRepository extends JpaRepository<PhotoLabKeyword, Long> {

    List<PhotoLabKeyword> findByPhotoLabIdIn(List<Long> photoLabIds);
}
