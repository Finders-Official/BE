package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.memberUser " +
            "WHERE c.post = :post AND c.status = 'ACTIVE' "+
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByPostOrderByCreatedAtDesc(@Param("post") Post post);

    long countByPostAndStatus(Post post, CommunityStatus status);

    List<Comment> findAllByPostAndStatusOrderByCreatedAtDesc(Post post, CommunityStatus status);
}