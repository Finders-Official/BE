package com.finders.api.domain.inquiry.controller;

import com.finders.api.domain.inquiry.dto.request.InquiryRequest;
import com.finders.api.domain.inquiry.dto.response.InquiryResponse;
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

@Tag(name = "Inquiry", description = "1:1 문의 API (User)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryQueryService inquiryQueryService;
    private final InquiryCommandService inquiryCommandService;

    @Operation(summary = "내 문의 목록 조회", description = "로그인한 사용자의 문의 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<InquiryResponse.InquiryListDTO> getMyInquiries(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_FOUND,
                inquiryQueryService.getMyInquiries(authUser.memberId(), page, size)
        );
    }

    @Operation(summary = "문의 상세 조회", description = "특정 문의의 상세 정보를 조회합니다.")
    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryResponse.InquiryDetailDTO> getInquiryDetail(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_FOUND,
                inquiryQueryService.getInquiryDetail(inquiryId, authUser.memberId())
        );
    }

    @Operation(summary = "문의 생성", description = "새로운 문의를 등록합니다. photoLabId가 없으면 고객센터 문의, 있으면 현상소 문의입니다.")
    @PostMapping
    public ApiResponse<InquiryResponse.InquiryCreateDTO> createInquiry(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody InquiryRequest.CreateInquiryDTO request
    ) {
        return ApiResponse.success(
                SuccessCode.INQUIRY_CREATED,
                inquiryCommandService.createInquiry(request, authUser.memberId())
        );
    }
}
