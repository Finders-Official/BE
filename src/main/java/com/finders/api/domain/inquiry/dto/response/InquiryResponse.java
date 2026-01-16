package com.finders.api.domain.inquiry.dto.response;

import com.finders.api.domain.inquiry.entity.Inquiry;
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
            boolean hasReply,
            ReplyPreviewDTO latestReply
    ) {
        public static InquiryItemDTO from(Inquiry inquiry) {
            InquiryReply latestReply = inquiry.getReplies().isEmpty()
                    ? null
                    : inquiry.getReplies().get(inquiry.getReplies().size() - 1);

            return InquiryItemDTO.builder()
                    .id(inquiry.getId())
                    .title(inquiry.getTitle())
                    .content(inquiry.getContent())
                    .status(inquiry.getStatus())
                    .photoLabName(inquiry.getPhotoLab() != null ? inquiry.getPhotoLab().getName() : null)
                    .createdAt(inquiry.getCreatedAt())
                    .hasReply(!inquiry.getReplies().isEmpty())
                    .latestReply(latestReply != null ? ReplyPreviewDTO.from(latestReply) : null)
                    .build();
        }
    }

    /**
     * 답변 미리보기 (목록용)
     */
    @Builder
    public record ReplyPreviewDTO(
            Long id,
            String content,
            LocalDateTime createdAt
    ) {
        public static ReplyPreviewDTO from(InquiryReply reply) {
            return ReplyPreviewDTO.builder()
                    .id(reply.getId())
                    .content(reply.getContent())
                    .createdAt(reply.getCreatedAt())
                    .build();
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
            LocalDateTime createdAt,
            List<ReplyDTO> replies
    ) {
        public static InquiryDetailDTO from(Inquiry inquiry) {
            return InquiryDetailDTO.builder()
                    .id(inquiry.getId())
                    .title(inquiry.getTitle())
                    .content(inquiry.getContent())
                    .status(inquiry.getStatus())
                    .photoLab(inquiry.getPhotoLab() != null ? PhotoLabInfoDTO.from(inquiry) : null)
                    .member(MemberInfoDTO.from(inquiry))
                    .createdAt(inquiry.getCreatedAt())
                    .replies(inquiry.getReplies().stream().map(ReplyDTO::from).toList())
                    .build();
        }
    }

    /**
     * 현상소 정보
     */
    @Builder
    public record PhotoLabInfoDTO(
            Long id,
            String name
    ) {
        public static PhotoLabInfoDTO from(Inquiry inquiry) {
            return PhotoLabInfoDTO.builder()
                    .id(inquiry.getPhotoLab().getId())
                    .name(inquiry.getPhotoLab().getName())
                    .build();
        }
    }

    /**
     * 회원 정보 (Owner/Admin 조회용)
     */
    @Builder
    public record MemberInfoDTO(
            Long id,
            String name
    ) {
        public static MemberInfoDTO from(Inquiry inquiry) {
            return MemberInfoDTO.builder()
                    .id(inquiry.getMember().getId())
                    .name(inquiry.getMember().getName())
                    .build();
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
     * 문의 생성 응답
     */
    @Builder
    public record InquiryCreateDTO(
            Long id,
            String message
    ) {
        public static InquiryCreateDTO from(Inquiry inquiry) {
            return InquiryCreateDTO.builder()
                    .id(inquiry.getId())
                    .message("문의가 등록되었습니다.")
                    .build();
        }
    }

    /**
     * 답변 생성 응답
     */
    @Builder
    public record ReplyCreateDTO(
            Long id,
            String message
    ) {
        public static ReplyCreateDTO from(InquiryReply reply) {
            return ReplyCreateDTO.builder()
                    .id(reply.getId())
                    .message("답변이 등록되었습니다.")
                    .build();
        }
    }
}
