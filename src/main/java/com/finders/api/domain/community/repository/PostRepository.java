package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}