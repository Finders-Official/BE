package com.finders.api.domain.inquiry.controller;

import com.finders.api.domain.inquiry.dto.request.InquiryRequest;
import com.finders.api.domain.inquiry.dto.response.InquiryResponse;
import com.finders.api.domain.inquiry.enums.InquiryStatus;
import com.finders.api.domain.inquiry.service.command.InquiryCommandService;
import com.finders.api.domain.inquiry.service.query.InquiryQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Owner Inquiry", description = "1:1 문의 API (Owner)")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@RequestMapping("/owner/inquiries")
public class OwnerInquiryController {

    private final InquiryQueryService inquiryQueryService;
    private final InquiryCommandService inquiryCommandService;

    @Operation(summary = "현상소 문의 목록 조회", description = "내 현상소에 들어온 문의 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<InquiryResponse.InquiryListDTO> getPhotoLabInquiries(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long photoLabId,
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_FOUND,
                inquiryQueryService.getPhotoLabInquiries(photoLabId, authUser.memberId(), status, page, size)
        );
    }

    @Operation(summary = "현상소 문의 상세 조회", description = "특정 문의의 상세 정보를 조회합니다.")
    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryResponse.InquiryDetailDTO> getPhotoLabInquiryDetail(
            @PathVariable Long inquiryId,
            @RequestParam Long photoLabId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_FOUND,
                inquiryQueryService.getPhotoLabInquiryDetail(inquiryId, photoLabId, authUser.memberId())
        );
    }

    @Operation(summary = "문의 답변 작성", description = "현상소 문의에 답변을 작성합니다.")
    @PostMapping("/{inquiryId}/replies")
    public ApiResponse<InquiryResponse.ReplyCreateDTO> createReply(
            @PathVariable Long inquiryId,
            @RequestParam Long photoLabId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody InquiryRequest.CreateReplyDTO request
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_REPLY_CREATED,
                inquiryCommandService.createPhotoLabReply(inquiryId, photoLabId, request, authUser.memberId())
        );
    }
}
