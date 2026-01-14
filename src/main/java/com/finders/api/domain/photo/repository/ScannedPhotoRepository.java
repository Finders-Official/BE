package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.ScannedPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScannedPhotoRepository extends JpaRepository<ScannedPhoto, Long> {
}
