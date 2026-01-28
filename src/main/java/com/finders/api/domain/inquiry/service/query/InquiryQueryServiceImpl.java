package com.finders.api.domain.inquiry.service.query;

import com.finders.api.domain.inquiry.dto.response.InquiryResponse;
import com.finders.api.domain.inquiry.entity.Inquiry;
import com.finders.api.domain.inquiry.entity.InquiryImage;
import com.finders.api.domain.inquiry.enums.InquiryStatus;
import com.finders.api.domain.inquiry.repository.InquiryQueryRepository;
import com.finders.api.domain.inquiry.repository.InquiryRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryServiceImpl implements InquiryQueryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryQueryRepository inquiryQueryRepository;
    private final PhotoLabRepository photoLabRepository;
    private final StorageService storageService;

    @Override
    public InquiryResponse.InquiryListDTO getMyInquiries(Long memberId, int page, int size) {
        List<Inquiry> inquiries = inquiryQueryRepository.findByMemberId(memberId, page, size);
        long totalCount = inquiryQueryRepository.countByMemberId(memberId);

        return InquiryResponse.InquiryListDTO.from(inquiries, totalCount, page, size);
    }

    @Override
    public InquiryResponse.InquiryDetailDTO getInquiryDetail(Long inquiryId, Long memberId) {
        Inquiry inquiry = inquiryRepository.findByIdWithDetails(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }

        return createInquiryDetailDTOWithPublicUrls(inquiry);
    }

    @Override
    public InquiryResponse.InquiryListDTO getPhotoLabInquiries(Long photoLabId, Long ownerId, InquiryStatus status, int page, int size) {
        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLab.getOwner() == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        List<Inquiry> inquiries = inquiryQueryRepository.findByPhotoLabId(photoLabId, status, page, size);
        long totalCount = inquiryQueryRepository.countByPhotoLabId(photoLabId, status);

        return InquiryResponse.InquiryListDTO.from(inquiries, totalCount, page, size);
    }

    @Override
    public InquiryResponse.InquiryDetailDTO getPhotoLabInquiryDetail(Long inquiryId, Long photoLabId, Long ownerId) {
        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLab.getOwner() == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        Inquiry inquiry = inquiryRepository.findByIdWithDetails(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (inquiry.getPhotoLab() == null || !inquiry.getPhotoLab().getId().equals(photoLabId)) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }

        return createInquiryDetailDTOWithPublicUrls(inquiry);
    }

    @Override
    public InquiryResponse.InquiryListDTO getServiceInquiries(InquiryStatus status, int page, int size) {
        List<Inquiry> inquiries = inquiryQueryRepository.findServiceInquiries(status, page, size);
        long totalCount = inquiryQueryRepository.countServiceInquiries(status);

        return InquiryResponse.InquiryListDTO.from(inquiries, totalCount, page, size);
    }

    @Override
    public InquiryResponse.InquiryDetailDTO getServiceInquiryDetail(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findByIdWithDetails(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.isServiceInquiry()) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }

        return createInquiryDetailDTOWithPublicUrls(inquiry);
    }

    private InquiryResponse.InquiryDetailDTO createInquiryDetailDTOWithPublicUrls(Inquiry inquiry) {
        List<String> imageUrls = inquiry.getImages().stream()
                .map(InquiryImage::getObjectPath)
                .map(storageService::getPublicUrl)
                .toList();

        log.debug("[InquiryQueryServiceImpl] objectPath → Public URL 변환 완료: inquiryId={}, imageCount={}",
                inquiry.getId(), imageUrls.size());

        return InquiryResponse.InquiryDetailDTO.fromWithUrls(inquiry, imageUrls);
    }
}
