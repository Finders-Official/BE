package com.finders.api.domain.inquiry.service.query;

import com.finders.api.domain.inquiry.dto.response.InquiryResponse;
import com.finders.api.domain.inquiry.enums.InquiryStatus;

public interface InquiryQueryService {

    /**
     * User: 내 문의 목록 조회
     */
    InquiryResponse.InquiryListDTO getMyInquiries(Long memberId, int page, int size);

    /**
     * User: 문의 상세 조회
     */
    InquiryResponse.InquiryDetailDTO getInquiryDetail(Long inquiryId, Long memberId);

    /**
     * Owner: 현상소 문의 목록 조회
     */
    InquiryResponse.InquiryListDTO getPhotoLabInquiries(Long photoLabId, Long ownerId, InquiryStatus status, int page, int size);

    /**
     * Owner: 현상소 문의 상세 조회
     */
    InquiryResponse.InquiryDetailDTO getPhotoLabInquiryDetail(Long inquiryId, Long photoLabId, Long ownerId);

    /**
     * Admin: 서비스 문의 목록 조회
     */
    InquiryResponse.InquiryListDTO getServiceInquiries(InquiryStatus status, int page, int size);

    /**
     * Admin: 서비스 문의 상세 조회
     */
    InquiryResponse.InquiryDetailDTO getServiceInquiryDetail(Long inquiryId);
}
