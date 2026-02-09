package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.PhotoRequest;
import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.dto.PhotoResponse.ScanResult;
import com.finders.api.domain.photo.service.command.PhotoCommandService;
import com.finders.api.domain.photo.service.query.PhotoQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SlicedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Photo", description = "회원용 현상/스캔/인화 내역 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoCommandService photoCommandService;
    private final PhotoQueryService photoQueryService;

    @Operation(
            summary = "회원 - 내 진행중 작업 조회",
            description = "진행중인 현상/스캔/인화(배송/수령 포함) 작업이 있으면 진행 상태를 내려줍니다. 없으면 data는 null입니다."
    )
    @GetMapping("/me/work-in-progress")
    public ApiResponse<PhotoResponse.MyCurrentWork> getMyCurrentWork(
            @AuthenticationPrincipal AuthUser user
    ) {
        PhotoResponse.MyCurrentWork result =
                photoQueryService.getMyCurrentWork(user.memberId());

        return ApiResponse.success(SuccessCode.OK, result);
    }

    @Operation(
            summary = "회원 - 내 현상 주문 목록 조회(무한스크롤)",
            description = "내 현상 주문을 Slice 기반으로 조회합니다. 각 주문은 스캔 프리뷰 signedUrl을 포함합니다. 인화 주문이면 배송 정보도 포함될 수 있습니다."
    )
    @GetMapping("/development-orders")
    public SlicedResponse<PhotoResponse.MyDevelopmentOrder> getMyDevelopmentOrders(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Slice<PhotoResponse.MyDevelopmentOrder> result =
                photoQueryService.getMyDevelopmentOrders(user.memberId(), page, size);

        return SlicedResponse.of(SuccessCode.OK, result);
    }

    @Operation(
            summary = "회원 - 스캔 결과 사진 목록 조회",
            description = "해당 현상 주문의 스캔 결과 사진을 무한스크롤로 조회합니다. 각 항목은 signedUrl을 포함합니다."
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

    @Operation(
            summary = "회원 - 인화 안함 확정",
            description = "해당 현상 주문에 대해 인화를 진행하지 않기로 확정하고 주문 상태를 완료 처리합니다."
    )
    @PostMapping("/development-orders/{developmentOrderId}/print/skip")
    public ApiResponse<Long> skipPrint(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long developmentOrderId
    ) {
        Long result = photoCommandService.skipPrint(user.memberId(), developmentOrderId);
        return ApiResponse.success(SuccessCode.OK, result);
    }


    @Operation(
            summary = "회원 - 인화 옵션 목록 조회",
            description = "인화 옵션(필름/인화방식/인화지/사이즈/인화유형)과 배송비 정책을 내려줍니다."
    )
    @GetMapping("/print-orders/options")
    public ApiResponse<PhotoResponse.PrintOptions> getPrintOptions(
            @AuthenticationPrincipal AuthUser user
    ) {

        return ApiResponse.success(SuccessCode.OK, photoQueryService.getPrintOptions());
    }
    @Operation(
            summary = "회원 - 인화 옵션에 따른 가격 조회",
            description = "인화 옵션(필름/인화방식/인화지/사이즈/인화유형)과 배송비 정책에 따른 가격을 내려줍니다."
    )
    @PostMapping("/print-orders/quote")
    public ApiResponse<PhotoResponse.PrintQuote> quotePrintPrice(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid PhotoRequest.PrintQuote request
    ) {
        PhotoResponse.PrintQuote result = photoQueryService.quote(user.memberId(), request);
        return ApiResponse.success(SuccessCode.OK, result);
    }

    @Operation(
            summary = "회원 - 인화 주문 생성",
            description = "선택한 스캔 사진과 인화 옵션으로 인화 주문을 생성합니다."
    )
    @PostMapping("/print-orders")
    public ApiResponse<Long> createPrintOrder(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid PhotoRequest.PrintQuote request
    ) {
        Long printOrderId = photoCommandService.createPrintOrder(user.memberId(), request);
        return ApiResponse.success(SuccessCode.CREATED, printOrderId);
    }

    @Operation(
            summary = "회원 - 현상 주문한 현상소 사업자 계좌 조회",
            description = "developmentOrderId에 연결된 현상소(오너)의 입금 계좌 정보를 조회합니다."
    )
    @GetMapping("/development-orders/{developmentOrderId}/photo-labs/account")
    public ApiResponse<PhotoResponse.PhotoLabAccount> getPhotoLabAccount(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long developmentOrderId
    ) {
        PhotoResponse.PhotoLabAccount result =
                photoQueryService.getPhotoLabAccount(user.memberId(), developmentOrderId);

        return ApiResponse.success(SuccessCode.OK, result);
    }

    @Operation(
            summary = "회원 - 입금 캡처 등록 확정",
            description = "프론트가 업로드한 objectPath와 입금자/은행 정보를 저장하고 인화 주문 상태를 변경합니다."
    )
    @PostMapping("/print-orders/{printOrderId}/deposit-receipt")
    public ApiResponse<Long> confirmDepositReceipt(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long printOrderId,
            @RequestBody @Valid PhotoRequest.DepositReceiptConfirm request
    ) {
        Long result = photoCommandService.confirmDepositReceipt(
                user.memberId(),
                printOrderId,
                request
        );
        return ApiResponse.success(SuccessCode.OK, result);
    }

    @Operation(
            summary = "회원 - 스캔/현상 결과 수령 확정",
            description = "developmentOrder가 SCANNING 상태일 때 회원이 수령을 확정하면 COMPLETED로 변경합니다."
    )
    @PostMapping("/development-orders/{developmentOrderId}/receipt")
    public ApiResponse<Long> confirmReceive(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long developmentOrderId
    ) {
        Long result = photoCommandService.confirmReceive(user.memberId(), developmentOrderId);
        return ApiResponse.success(SuccessCode.OK, result);
    }
}
