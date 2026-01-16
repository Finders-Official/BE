package com.finders.api.domain.inquiry.service.command;

import com.finders.api.domain.inquiry.dto.request.InquiryRequest;
import com.finders.api.domain.inquiry.dto.response.InquiryResponse;
import com.finders.api.domain.inquiry.entity.Inquiry;
import com.finders.api.domain.inquiry.entity.InquiryReply;
import com.finders.api.domain.inquiry.enums.InquiryStatus;
import com.finders.api.domain.inquiry.repository.InquiryRepository;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommandServiceImpl implements InquiryCommandService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final PhotoLabRepository photoLabRepository;

    @Override
    public InquiryResponse.InquiryCreateDTO createInquiry(InquiryRequest.CreateInquiryDTO request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        PhotoLab photoLab = null;
        if (request.photoLabId() != null) {
            photoLab = photoLabRepository.findById(request.photoLabId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        }

        Inquiry inquiry = Inquiry.create(member, photoLab, request.title(), request.content());
        inquiryRepository.save(inquiry);

        return InquiryResponse.InquiryCreateDTO.from(inquiry);
    }

    @Override
    public InquiryResponse.ReplyCreateDTO createPhotoLabReply(Long inquiryId, InquiryRequest.CreateReplyDTO request, Long ownerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (inquiry.getStatus() == InquiryStatus.CLOSED) {
            throw new CustomException(ErrorCode.INQUIRY_ALREADY_CLOSED);
        }

        PhotoLab photoLab = photoLabRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (inquiry.getPhotoLab() == null || !inquiry.getPhotoLab().getId().equals(photoLab.getId())) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }

        Member replier = memberRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        InquiryReply reply = InquiryReply.create(inquiry, replier, request.content());

        return InquiryResponse.ReplyCreateDTO.from(reply);
    }

    @Override
    public InquiryResponse.ReplyCreateDTO createServiceReply(Long inquiryId, InquiryRequest.CreateReplyDTO request, Long adminId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (inquiry.getStatus() == InquiryStatus.CLOSED) {
            throw new CustomException(ErrorCode.INQUIRY_ALREADY_CLOSED);
        }

        if (!inquiry.isServiceInquiry()) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }

        Member replier = memberRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        InquiryReply reply = InquiryReply.create(inquiry, replier, request.content());

        return InquiryResponse.ReplyCreateDTO.from(reply);
    }
}
