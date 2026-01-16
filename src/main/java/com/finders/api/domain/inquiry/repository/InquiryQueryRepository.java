package com.finders.api.domain.inquiry.repository;

import com.finders.api.domain.inquiry.entity.Inquiry;
import com.finders.api.domain.inquiry.enums.InquiryStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.finders.api.domain.inquiry.entity.QInquiry.inquiry;
import static com.finders.api.domain.inquiry.entity.QInquiryReply.inquiryReply;

@Repository
@RequiredArgsConstructor
public class InquiryQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * User의 문의 목록 조회 (최신순)
     */
    public List<Inquiry> findByMemberId(Long memberId, int page, int size) {
        return queryFactory
                .selectFrom(inquiry)
                .leftJoin(inquiry.photoLab).fetchJoin()
                .leftJoin(inquiry.replies, inquiryReply).fetchJoin()
                .where(inquiry.member.id.eq(memberId))
                .orderBy(inquiry.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    /**
     * User의 문의 총 개수
     */
    public long countByMemberId(Long memberId) {
        Long count = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(inquiry.member.id.eq(memberId))
                .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * Owner의 현상소 문의 목록 조회 (현상소별)
     */
    public List<Inquiry> findByPhotoLabId(Long photoLabId, InquiryStatus status, int page, int size) {
        return queryFactory
                .selectFrom(inquiry)
                .leftJoin(inquiry.replies, inquiryReply).fetchJoin()
                .where(
                        inquiry.photoLab.id.eq(photoLabId),
                        statusEq(status)
                )
                .orderBy(inquiry.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    /**
     * Owner의 현상소 문의 총 개수
     */
    public long countByPhotoLabId(Long photoLabId, InquiryStatus status) {
        Long count = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(
                        inquiry.photoLab.id.eq(photoLabId),
                        statusEq(status)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * Admin의 서비스 문의 목록 조회 (photoLabId가 null인 문의)
     */
    public List<Inquiry> findServiceInquiries(InquiryStatus status, int page, int size) {
        return queryFactory
                .selectFrom(inquiry)
                .leftJoin(inquiry.replies, inquiryReply).fetchJoin()
                .where(
                        inquiry.photoLab.isNull(),
                        statusEq(status)
                )
                .orderBy(inquiry.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    /**
     * Admin의 서비스 문의 총 개수
     */
    public long countServiceInquiries(InquiryStatus status) {
        Long count = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(
                        inquiry.photoLab.isNull(),
                        statusEq(status)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression statusEq(InquiryStatus status) {
        return status != null ? inquiry.status.eq(status) : null;
    }
}
