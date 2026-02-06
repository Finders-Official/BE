package com.finders.api.domain.store.repository;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
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
import java.time.LocalTime;
import java.util.List;

import static com.finders.api.domain.reservation.entity.QReservationSlot.reservationSlot;
import static com.finders.api.domain.reservation.policy.ReservationPolicy.TIME_INTERVAL_MINUTES;
import static com.finders.api.domain.store.entity.QPhotoLab.photoLab;
import static com.finders.api.domain.store.entity.QPhotoLabBusinessHour.photoLabBusinessHour;
import static com.finders.api.domain.store.entity.QPhotoLabTag.photoLabTag;

@Repository
@RequiredArgsConstructor
public class PhotoLabQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final int AUTOCOMPLETE_LIMIT = 4;

    public Page<PhotoLab> search(
            String query,
            List<Long> tagIds,
            List<Long> regionIds,
            LocalDate date,
            LocalTime time,
            int page,
            int size,
            Double lat,
            Double lng,
            boolean useDistance
    ) {
        BooleanExpression condition = photoLab.status.eq(PhotoLabStatus.ACTIVE)
                .and(likeQuery(query))
                .and(inRegion(regionIds))
                .and(hasTagIds(tagIds))
                .and(isOpenAndReservable(date, time));

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

    public List<String> autocompletePhotoLabNames(String keyword) {
        return queryFactory
                .select(photoLab.name).distinct()
                .from(photoLab)
                .where(
                        photoLab.status.eq(PhotoLabStatus.ACTIVE),
                        photoLab.name.startsWith(keyword)
                )
                .orderBy(
                        new CaseBuilder()
                                .when(photoLab.name.eq(keyword)).then(0)
                                .otherwise(1).asc(),
                        photoLab.name.length().asc(),
                        photoLab.name.asc()
                )
                .limit(AUTOCOMPLETE_LIMIT)
                .fetch();
    }

    private BooleanExpression likeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return photoLab.name.containsIgnoreCase(query)
                .or(photoLab.address.containsIgnoreCase(query));
    }

    private BooleanExpression inRegion(List<Long> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) {
            return null;
        }
        return photoLab.region.id.in(regionIds);
    }

    private BooleanExpression hasTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        return JPAExpressions
                .selectOne()
                .from(photoLabTag)
                .where(photoLabTag.photoLab.eq(photoLab)
                        .and(photoLabTag.tag.id.in(tagIds)))
                .exists();
    }

    private BooleanExpression isOpenAndReservable(LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        BooleanExpression openCondition = photoLabBusinessHour.photoLab.eq(photoLab)
                .and(photoLabBusinessHour.dayOfWeek.eq(dayOfWeek))
                .and(photoLabBusinessHour.isClosed.isFalse());

        if (time != null) {
            openCondition = openCondition
                    .and(photoLabBusinessHour.openTime.loe(time))
                    .and(photoLabBusinessHour.closeTime.goe(time.plusMinutes(TIME_INTERVAL_MINUTES)));
        }

        BooleanExpression open = JPAExpressions
                .selectOne()
                .from(photoLabBusinessHour)
                .where(openCondition)
                .exists();

        BooleanExpression reservable;
        if (time != null) {
            BooleanExpression fullAtTime = JPAExpressions
                    .selectOne()
                    .from(reservationSlot)
                    .where(reservationSlot.photoLab.eq(photoLab)
                            .and(reservationSlot.reservationDate.eq(date))
                            .and(reservationSlot.reservationTime.eq(time))
                            .and(reservationSlot.reservedCount.goe(reservationSlot.maxCapacity)))
                    .exists();
            reservable = fullAtTime.not();
        } else {
            BooleanExpression availableSlotExists = JPAExpressions
                    .selectOne()
                    .from(reservationSlot)
                    .where(reservationSlot.photoLab.eq(photoLab)
                            .and(reservationSlot.reservationDate.eq(date))
                            .and(reservationSlot.reservedCount.lt(reservationSlot.maxCapacity)))
                    .exists();

            BooleanExpression noSlotExists = JPAExpressions
                    .selectOne()
                    .from(reservationSlot)
                    .where(reservationSlot.photoLab.eq(photoLab)
                            .and(reservationSlot.reservationDate.eq(date)))
                    .exists()
                    .not();

            reservable = availableSlotExists.or(noSlotExists);
        }

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
