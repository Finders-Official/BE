package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

import static com.finders.api.domain.community.entity.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Post> findAllForFeed() {
        return queryFactory
                .selectFrom(post)
                .where(post.status.eq(CommunityStatus.ACTIVE))
                .orderBy(post.createdAt.desc())
                .fetch();
    }
}