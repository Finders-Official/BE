package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostOrderByCreatedAtDesc(Post post);

    long countByPost(Post post);
}