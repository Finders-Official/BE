package com.finders.api.domain.inquiry.service.command;

import com.finders.api.domain.inquiry.dto.request.InquiryRequest;
import com.finders.api.domain.inquiry.dto.response.InquiryResponse;

public interface InquiryCommandService {

    /**
     * User: 문의 생성
     */
    InquiryResponse.InquiryCreateDTO createInquiry(InquiryRequest.CreateInquiryDTO request, Long memberId);

    /**
     * Owner: 현상소 문의 답변
     */
    InquiryResponse.ReplyCreateDTO createPhotoLabReply(Long inquiryId, Long photoLabId, InquiryRequest.CreateReplyDTO request, Long ownerId);

    /**
     * Admin: 서비스 문의 답변
     */
    InquiryResponse.ReplyCreateDTO createServiceReply(Long inquiryId, InquiryRequest.CreateReplyDTO request, Long adminId);
}
