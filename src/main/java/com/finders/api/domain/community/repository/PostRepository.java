package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.memberUser " +
            "LEFT JOIN FETCH p.photoLab " +
            "WHERE p.id = :postId AND p.status = 'ACTIVE'")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    // 해당 키워드에서 좋아요 많이 받은 사진 검색 (최근 검색어)
    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'ACTIVE' " +
            "AND p.title LIKE %:keyword% " +
            "ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findTopPostsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    default Optional<Post> findTopByKeywordOrderByLikes(String keyword) {
        List<Post> results = findTopPostsByKeyword(keyword, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}