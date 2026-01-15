package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabTag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.finders.api.domain.store.entity.QPhotoLabTag.photoLabTag;

@Repository
@RequiredArgsConstructor
public class PhotoLabTagQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PhotoLabTag> findByPhotoLabIds(List<Long> photoLabIds) {
        if (photoLabIds == null || photoLabIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(photoLabTag)
                .join(photoLabTag.tag).fetchJoin()
                .where(photoLabTag.photoLab.id.in(photoLabIds))
                .fetch();
    }
}
