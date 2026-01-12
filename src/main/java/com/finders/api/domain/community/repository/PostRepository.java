package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.memberUser " +
            "LEFT JOIN FETCH p.photoLab " +
            "WHERE p.id = :id AND p.status = 'ACTIVE'")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);

    // 커뮤니티 게시글 검색
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.photoLab pl " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (p.title LIKE %:keyword% " +
            "OR p.content LIKE %:keyword% " +
            "OR pl.name LIKE %:keyword%)")
    Page<Post> searchPostsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}