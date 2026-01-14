package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static com.finders.api.domain.reservation.entity.QReservationSlot.reservationSlot;
import static com.finders.api.domain.store.entity.QPhotoLab.photoLab;
import static com.finders.api.domain.store.entity.QPhotoLabBusinessHour.photoLabBusinessHour;
import static com.finders.api.domain.store.entity.QPhotoLabKeyword.photoLabKeyword;

@Repository
@RequiredArgsConstructor
public class PhotoLabQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<PhotoLab> search(
            String query,
            List<Long> keywordIds,
            Long regionId,
            LocalDate date,
            int page,
            int size,
            Double lat,
            Double lng,
            boolean useDistance
    ) {
        BooleanExpression condition = photoLab.status.eq(PhotoLabStatus.ACTIVE)
                .and(likeQuery(query))
                .and(inRegion(regionId))
                .and(hasKeywordIds(keywordIds))
                .and(isOpenAndReservable(date));

        JPAQuery<PhotoLab> contentQuery = queryFactory
                .selectFrom(photoLab)
                .where(condition)
                .orderBy(orderSpecifiers(lat, lng, useDistance))
                .offset((long) page * size)
                .limit(size);

        List<PhotoLab> content = contentQuery.fetch();

        Long total = queryFactory
                .select(photoLab.count())
                .from(photoLab)
                .where(condition)
                .fetchOne();

        long totalElements = total != null ? total : 0L;

        return new PageImpl<>(content, org.springframework.data.domain.PageRequest.of(page, size), totalElements);
    }

    private BooleanExpression likeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return photoLab.name.containsIgnoreCase(query)
                .or(photoLab.address.containsIgnoreCase(query));
    }

    private BooleanExpression inRegion(Long regionId) {
        if (regionId == null) {
            return null;
        }
        return photoLab.region.id.eq(regionId);
    }

    private BooleanExpression hasKeywordIds(List<Long> keywordIds) {
        if (keywordIds == null || keywordIds.isEmpty()) {
            return null;
        }
        return JPAExpressions
                .selectOne()
                .from(photoLabKeyword)
                .where(photoLabKeyword.photoLab.eq(photoLab)
                        .and(photoLabKeyword.id.in(keywordIds)))
                .exists();
    }

    private BooleanExpression isOpenAndReservable(LocalDate date) {
        if (date == null) {
            return null;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        BooleanExpression open = JPAExpressions
                .selectOne()
                .from(photoLabBusinessHour)
                .where(photoLabBusinessHour.photoLab.eq(photoLab)
                        .and(photoLabBusinessHour.dayOfWeek.eq(dayOfWeek))
                        .and(photoLabBusinessHour.isClosed.isFalse()))
                .exists();

        BooleanExpression reservable = JPAExpressions
                .selectOne()
                .from(reservationSlot)
                .where(reservationSlot.photoLab.eq(photoLab)
                        .and(reservationSlot.reservationDate.eq(date))
                        .and(reservationSlot.reservedCount.lt(reservationSlot.maxCapacity)))
                .exists();

        return open.and(reservable);
    }

    private OrderSpecifier<?>[] orderSpecifiers(Double lat, Double lng, boolean useDistance) {
        if (useDistance && lat != null && lng != null) {
            return new OrderSpecifier<?>[]{
                    distanceExpression(lat, lng).asc(),
                    photoLab.workCount.desc(),
                    photoLab.id.asc()
            };
        }
        return new OrderSpecifier<?>[]{
                photoLab.workCount.desc(),
                photoLab.id.asc()
        };
    }

    private com.querydsl.core.types.dsl.NumberExpression<Double> distanceExpression(Double lat, Double lng) {
        return Expressions.numberTemplate(
                Double.class,
                "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
                Expressions.constant(lat),
                photoLab.latitude,
                Expressions.constant(lng),
                photoLab.longitude
        );
    }
}
