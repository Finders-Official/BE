package com.finders.api.domain.inquiry.repository;

import com.finders.api.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("SELECT i FROM Inquiry i " +
            "LEFT JOIN FETCH i.member " +
            "LEFT JOIN FETCH i.photoLab " +
            "LEFT JOIN FETCH i.replies r " +
            "LEFT JOIN FETCH r.replier " +
            "WHERE i.id = :id")
    Optional<Inquiry> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT i FROM Inquiry i " +
            "LEFT JOIN FETCH i.photoLab " +
            "WHERE i.id = :id")
    Optional<Inquiry> findByIdWithPhotoLab(@Param("id") Long id);
}
