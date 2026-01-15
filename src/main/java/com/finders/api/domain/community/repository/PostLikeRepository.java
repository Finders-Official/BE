package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostLike;
import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndMemberUser(Post post, MemberUser memberUser);

    Optional<PostLike> findByPostAndMemberUser(Post post, MemberUser memberUser);

    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.memberUser.id = :memberId AND pl.post.id IN :postIds")
    Set<Long> findLikedPostIdsByMemberAndPostIds(@Param("memberId") Long memberId, @Param("postIds") List<Long> postIds);
}