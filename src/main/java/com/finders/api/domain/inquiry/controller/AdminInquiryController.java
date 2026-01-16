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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Inquiry", description = "1:1 문의 API (Admin)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/inquiries")
public class AdminInquiryController {

    private final InquiryQueryService inquiryQueryService;
    private final InquiryCommandService inquiryCommandService;

    @Operation(summary = "서비스 문의 목록 조회", description = "파인더스 고객센터 문의 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<InquiryResponse.InquiryListDTO> getServiceInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_FOUND,
                inquiryQueryService.getServiceInquiries(status, page, size)
        );
    }

    @Operation(summary = "서비스 문의 상세 조회", description = "특정 문의의 상세 정보를 조회합니다.")
    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryResponse.InquiryDetailDTO> getServiceInquiryDetail(
            @PathVariable Long inquiryId
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_FOUND,
                inquiryQueryService.getServiceInquiryDetail(inquiryId)
        );
    }

    @Operation(summary = "문의 답변 작성", description = "서비스 문의에 답변을 작성합니다.")
    @PostMapping("/{inquiryId}/replies")
    public ApiResponse<InquiryResponse.ReplyCreateDTO> createReply(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody InquiryRequest.CreateReplyDTO request
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_REPLY_CREATED,
                inquiryCommandService.createServiceReply(inquiryId, request, authUser.memberId())
        );
    }
}
