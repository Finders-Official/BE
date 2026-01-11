package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoLabDocumentRepository extends JpaRepository<PhotoLabDocument, Long> {
}
