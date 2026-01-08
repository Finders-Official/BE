package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member " +
            "LEFT JOIN FETCH p.photoLab " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);
}