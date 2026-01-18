package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.PrintOrder;
import com.finders.api.domain.photo.entity.PrintOrderPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintOrderPhotoRepository extends JpaRepository<PrintOrderPhoto,Long> {
}
