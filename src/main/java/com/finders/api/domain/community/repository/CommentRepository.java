package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.memberUser " +
            "WHERE c.post = :post AND c.status = :status " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByPostAndStatusOrderByCreatedAtDesc(
            @Param("post") Post post,
            @Param("status") CommunityStatus status);
}