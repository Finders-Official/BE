package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostLike;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndMemberUser(Post post, MemberUser memberUser);

    Optional<PostLike> findByPostAndMemberUser(Post post, MemberUser memberUser);

    long countByPost(Post post);
}