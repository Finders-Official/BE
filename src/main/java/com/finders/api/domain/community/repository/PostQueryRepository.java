package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.enums.PostSearchFilter;
import com.finders.api.domain.store.entity.QPhotoLab;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

import static com.finders.api.domain.community.entity.QPost.post;
import static com.finders.api.domain.store.entity.QPhotoLab.photoLab;
import static com.finders.api.domain.community.entity.QPostLike.postLike;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final int POPULAR_POSTS_LIMIT = 10;

    public List<Post> findAllForFeed(int page, int size) {
        return queryFactory
                .selectFrom(post)
                .where(post.status.eq(CommunityStatus.ACTIVE))
                .orderBy(post.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    public List<Post> findTop10PopularPosts() {
        return queryFactory
                .selectFrom(post)
                .where(post.status.eq(CommunityStatus.ACTIVE))
                .orderBy(post.likeCount.desc(), post.createdAt.desc()) // 좋아요 순, 같으면 최신순
                .limit(POPULAR_POSTS_LIMIT)
                .fetch();
    }

    public List<Post> searchPosts(String keyword, PostSearchFilter filter, int page, int size) {
        return queryFactory
                .selectFrom(post)
                .leftJoin(post.photoLab, QPhotoLab.photoLab).fetchJoin()
                .where(
                        post.status.eq(CommunityStatus.ACTIVE),
                        createSearchCondition(filter, keyword)
                )
                .orderBy(post.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    public Long countSearchPosts(String keyword, PostSearchFilter filter) {
        return queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.status.eq(CommunityStatus.ACTIVE),
                        createSearchCondition(filter, keyword)
                )
                .fetchOne();
    }

    private BooleanExpression createSearchCondition(PostSearchFilter filter, String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        return switch (filter) {
            case TITLE -> post.title.containsIgnoreCase(keyword);
            case TITLE_CONTENT -> post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword));
            case LAB_NAME -> post.photoLab.name.containsIgnoreCase(keyword);
            case LAB_REVIEW -> post.labReview.containsIgnoreCase(keyword);
            default -> post.title.containsIgnoreCase(keyword);
        };
    }

    public Long countAllActivePosts() {
        return queryFactory
                .select(post.count())
                .from(post)
                .where(post.status.eq(CommunityStatus.ACTIVE))
                .fetchOne();
    }

    // 연관 검색어 관련
    public List<String> findTop3PhotoLabNames(String keyword) {
        return queryFactory
                .select(photoLab.name)
                .from(photoLab)
                .where(photoLab.name.containsIgnoreCase(keyword))
                .orderBy(photoLab.reviewCount.desc())
                .limit(3)
                .fetch();
    }

    public List<String> findTop10PostTitles(String keyword) {
        return queryFactory
                .select(post.title)
                .from(post)
                .where(
                        post.status.eq(CommunityStatus.ACTIVE),
                        post.title.containsIgnoreCase(keyword)
                )
                .orderBy(post.likeCount.desc())
                .limit(10)
                .fetch();
    }

    public List<Post> findLikedPostsByMemberId(Long memberId, int page, int size) {
        return queryFactory
                .selectFrom(post)
                .join(postLike).on(postLike.post.eq(post))
                .where(
                        postLike.memberUser.id.eq(memberId),
                        post.status.eq(CommunityStatus.ACTIVE)
                )
                .orderBy(postLike.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    public Long countMyLikedPosts(Long memberId) {
        return queryFactory
                .select(post.count())
                .from(postLike)
                .join(postLike.post, post)
                .where(
                        postLike.memberUser.id.eq(memberId),
                        post.status.eq(CommunityStatus.ACTIVE)
                )
                .fetchOne();
    }
    public List<Post> findByMemberId(Long memberId, int page, int size) {
        return queryFactory
                .selectFrom(post)
                .where(
                        post.memberUser.id.eq(memberId),
                        post.status.eq(CommunityStatus.ACTIVE)
                )
                .orderBy(post.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    public Long countByMemberId(Long memberId) {
        return queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.memberUser.id.eq(memberId),
                        post.status.eq(CommunityStatus.ACTIVE)
                )
                .fetchOne();
    }

}