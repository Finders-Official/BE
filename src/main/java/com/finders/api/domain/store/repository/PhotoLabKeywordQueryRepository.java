package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLabKeyword;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.finders.api.domain.store.entity.QPhotoLabKeyword.photoLabKeyword;

@Repository
@RequiredArgsConstructor
public class PhotoLabKeywordQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PhotoLabKeyword> findByPhotoLabIds(List<Long> photoLabIds) {
        if (photoLabIds == null || photoLabIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(photoLabKeyword)
                .where(photoLabKeyword.photoLab.id.in(photoLabIds))
                .fetch();
    }
}
