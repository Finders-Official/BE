package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabNotice;
import com.finders.api.domain.store.enums.NoticeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoLabNoticeRepository extends JpaRepository<PhotoLabNotice, Long> {

    Optional<PhotoLabNotice> findFirstByPhotoLab_IdAndIsActiveTrueOrderByCreatedAtDesc(Long photoLabId);
    Optional<PhotoLabNotice> findTopByPhotoLabIdAndNoticeTypeOrderByCreatedAtDesc(
            Long photoLabId,
            NoticeType noticeType
    );
}
