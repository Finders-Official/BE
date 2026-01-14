package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.dto.PhotoResponse.ScanResult;
import com.finders.api.domain.photo.service.query.PhotoQueryService;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SlicedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Photo", description = "회원용 현상/스캔/인화 내역 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoQueryService photoQueryService;

    @Operation(
            summary = "회원 - 내 지난 작업(현상 내역) 목록 조회",
            description = "최근 주문부터 페이지네이션(Page)으로 조회합니다."
    )
    @GetMapping("/development-orders")
    public PagedResponse<PhotoResponse.MyDevelopmentOrder> getMyDevelopmentOrders(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<PhotoResponse.MyDevelopmentOrder> result =
                photoQueryService.getMyDevelopmentOrders(user.memberId(), page, size);

        return PagedResponse.of(SuccessCode.OK, result);
    }

    @Operation(
            summary = "회원 - 스캔 결과 사진 목록 조회",
            description = "해당 현상 주문의 스캔 결과 사진을 페이지네이션(Slice)으로 조회합니다. 각 항목은 signedUrl을 포함합니다."
    )
    @GetMapping("/development-orders/{developmentOrderId}/scan-results")
    public SlicedResponse<PhotoResponse.ScanResult> getMyScanResults(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long developmentOrderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Slice<ScanResult> result =
                photoQueryService.getMyScanResults(user.memberId(), developmentOrderId, page, size);

        return SlicedResponse.of(SuccessCode.OK, result);
    }
}
