package com.finders.api.domain.inquiry.dto.response;

import com.finders.api.domain.inquiry.entity.Inquiry;
import com.finders.api.domain.inquiry.entity.InquiryImage;
import com.finders.api.domain.inquiry.entity.InquiryReply;
import com.finders.api.domain.inquiry.enums.InquiryStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryResponse {

    /**
     * 문의 목록 응답
     */
    @Builder
    public record InquiryListDTO(
            List<InquiryItemDTO> inquiries,
            long totalCount,
            int page,
            int size
    ) {
        public static InquiryListDTO from(List<Inquiry> inquiries, long totalCount, int page, int size) {
            return InquiryListDTO.builder()
                    .inquiries(inquiries.stream().map(InquiryItemDTO::from).toList())
                    .totalCount(totalCount)
                    .page(page)
                    .size(size)
                    .build();
        }
    }

    /**
     * 문의 목록 아이템
     */
    @Builder
    public record InquiryItemDTO(
            Long id,
            String title,
            String content,
            InquiryStatus status,
            String photoLabName,
            LocalDateTime createdAt,
            boolean hasReply
    ) {
        public static InquiryItemDTO from(Inquiry inquiry) {
            return InquiryItemDTO.builder()
                    .id(inquiry.getId())
                    .title(inquiry.getTitle())
                    .content(inquiry.getContent())
                    .status(inquiry.getStatus())
                    .photoLabName(inquiry.getPhotoLab() != null ? inquiry.getPhotoLab().getName() : null)
                    .createdAt(inquiry.getCreatedAt())
                    .hasReply(inquiry.getStatus() != InquiryStatus.PENDING)
                    .build();
        }
    }

    /**
     * 답변 미리보기 (목록용) - 필드 3개 이하 → 생성자 직접 사용
     */
    public record ReplyPreviewDTO(
            Long id,
            String content,
            LocalDateTime createdAt
    ) {
        public static ReplyPreviewDTO from(InquiryReply reply) {
            return new ReplyPreviewDTO(
                    reply.getId(),
                    reply.getContent(),
                    reply.getCreatedAt()
            );
        }
    }

    /**
     * 문의 상세 응답
     */
    @Builder
    public record InquiryDetailDTO(
            Long id,
            String title,
            String content,
            InquiryStatus status,
            PhotoLabInfoDTO photoLab,
            MemberInfoDTO member,
            List<String> imageUrls,
            LocalDateTime createdAt,
            List<ReplyDTO> replies
    ) {
        public static InquiryDetailDTO from(Inquiry inquiry) {
            return InquiryDetailDTO.builder()
                    .id(inquiry.getId())
                    .title(inquiry.getTitle())
                    .content(inquiry.getContent())
                    .status(inquiry.getStatus())
                    .photoLab(inquiry.getPhotoLab() != null ? PhotoLabInfoDTO.from(inquiry.getPhotoLab()) : null)
                    .member(MemberInfoDTO.from(inquiry))
                    .imageUrls(inquiry.getImages().stream().map(InquiryImage::getImageUrl).toList())
                    .createdAt(inquiry.getCreatedAt())
                    .replies(inquiry.getReplies().stream().map(ReplyDTO::from).toList())
                    .build();
        }
    }

    /**
     * 현상소 정보 - 필드 2개 → 생성자 직접 사용
     */
    public record PhotoLabInfoDTO(
            Long id,
            String name
    ) {
        public static PhotoLabInfoDTO from(com.finders.api.domain.store.entity.PhotoLab photoLab) {
            return new PhotoLabInfoDTO(
                    photoLab.getId(),
                    photoLab.getName()
            );
        }
    }

    /**
     * 회원 정보 (Owner/Admin 조회용) - 필드 2개 → 생성자 직접 사용
     */
    public record MemberInfoDTO(
            Long id,
            String name
    ) {
        public static MemberInfoDTO from(Inquiry inquiry) {
            return new MemberInfoDTO(
                    inquiry.getMember().getId(),
                    inquiry.getMember().getName()
            );
        }
    }

    /**
     * 답변 상세
     */
    @Builder
    public record ReplyDTO(
            Long id,
            String content,
            String replierName,
            LocalDateTime createdAt
    ) {
        public static ReplyDTO from(InquiryReply reply) {
            return ReplyDTO.builder()
                    .id(reply.getId())
                    .content(reply.getContent())
                    .replierName(reply.getReplier().getName())
                    .createdAt(reply.getCreatedAt())
                    .build();
        }
    }

    /**
     * 문의 생성 응답 - 필드 2개 → 생성자 직접 사용
     */
    public record InquiryCreateDTO(
            Long id,
            String message
    ) {
        public static InquiryCreateDTO from(Inquiry inquiry) {
            return new InquiryCreateDTO(
                    inquiry.getId(),
                    "문의가 등록되었습니다."
            );
        }
    }

    /**
     * 답변 생성 응답 - 필드 2개 → 생성자 직접 사용
     */
    public record ReplyCreateDTO(
            Long id,
            String message
    ) {
        public static ReplyCreateDTO from(InquiryReply reply) {
            return new ReplyCreateDTO(
                    reply.getId(),
                    "답변이 등록되었습니다."
            );
        }
    }
}
