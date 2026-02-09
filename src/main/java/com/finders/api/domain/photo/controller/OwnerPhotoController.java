package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.OwnerPhotoRequest;
import com.finders.api.domain.photo.dto.OwnerPhotoResponse;
import com.finders.api.domain.photo.service.command.OwnerPhotoCommandService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Slf4j
@Tag(name = "Owner Photo", description = "오너용 현상/스캔/인화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/photo-labs")
public class OwnerPhotoController {

    private final OwnerPhotoCommandService ownerPhotoCommandService;

    @Operation(
            summary = "오너 - 스캔 이미지를 업로드 할 presigned url 벌크 발급(PUT)",
            description = "private 버킷에 PUT 업로드 가능한 presigned url을 count 만큼 발급합니다."
    )
    @PostMapping("/{photoLabId}/scan-photos/presigned-urls")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.PresignedUrls> createScanUploadPresignedUrls(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @RequestBody @Valid OwnerPhotoRequest.CreateScanUploadPresignedUrls request
    ) {
        OwnerPhotoResponse.PresignedUrls response = ownerPhotoCommandService.createScanUploadPresignedUrls(
                photoLabId,
                owner.memberId(),
                request
        );
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @Operation(
            summary = "오너 - 현상 주문 생성",
            description = """
                    스캔 이미지는 사전에 private 버킷에 업로드되어 있어야 하며,
                    request.scannedPhotos의 imageKey를 통해 DB에만 저장합니다.
                    
                    - reservationId가 있으면 예약 기반
                    - 없으면 memberId 필수
                    """
    )
    @PostMapping("/{photoLabId}/development-orders")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.Created> createDevelopmentOrder(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @RequestBody @Valid OwnerPhotoRequest.CreateDevelopmentOrder request
    ) {
        Long orderId = ownerPhotoCommandService.createDevelopmentOrder(
                photoLabId,
                owner.memberId(),
                request
        );

        return ApiResponse.success(
                SuccessCode.OK,
                OwnerPhotoResponse.Created.of(orderId)
        );
    }

    @Operation(
            summary = "오너 - (주문 기준) 스캔 이미지 메타데이터 DB 등록",
            description = """
                    presigned url로 private 버킷에 업로드 완료한 뒤,
                    objectPath(imageKey)들을 scanned_photo 테이블에 일괄 저장합니다.
                    """
    )
    @PostMapping("/{photoLabId}/development-orders/{developmentOrderId}/scanned-photos")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.ScannedPhotosRegistered> registerScannedPhotos(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @PathVariable Long developmentOrderId,
            @RequestBody @Valid OwnerPhotoRequest.RegisterScannedPhotos request
    ) {
        OwnerPhotoResponse.ScannedPhotosRegistered response =
                ownerPhotoCommandService.registerScannedPhotos(
                        photoLabId,
                        owner.memberId(),
                        developmentOrderId,
                        request
                );

        return ApiResponse.success(SuccessCode.OK, response);
    }

    @Operation(
            summary = "오너 - 현상 주문 상태 변경",
            description = "오너가 현상 주문의 상태를 원하는 값으로 변경합니다."
    )
    @PatchMapping("/{photoLabId}/development-orders/{developmentOrderId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.DevelopmentOrderStatusUpdated> updateDevelopmentOrderStatus(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @PathVariable Long developmentOrderId,
            @RequestBody @Valid OwnerPhotoRequest.UpdateDevelopmentOrderStatus request
    ) {
        log.info("authUser = {}", owner);
        log.info("authorities = {}", owner.getAuthorities());

        OwnerPhotoResponse.DevelopmentOrderStatusUpdated response =
                ownerPhotoCommandService.updateDevelopmentOrderStatus(
                        photoLabId,
                        owner.memberId(),
                        developmentOrderId,
                        request
                );

        return ApiResponse.success(SuccessCode.OK, response);
    }

    @Operation(
            summary = "오너 - 인화 주문의 예상 완료 시간 등록",
            description = "오너가 인화주문을 확인하고 예상 완료 시간을 등록하여 CONFIRMED -> PRINTING으로 상태를 바꿉니다."
    )
    @PatchMapping("/{photoLabId}/print-orders/{printOrderId}/printing")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.PrintOrderStatusUpdated> startPrinting(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @PathVariable Long printOrderId,
            @RequestBody @Valid OwnerPhotoRequest.StartPrinting request
    ) {
        OwnerPhotoResponse.PrintOrderStatusUpdated result =
                ownerPhotoCommandService.startPrinting(photoLabId, owner.memberId(), printOrderId, request);
        return ApiResponse.success(SuccessCode.OK, result);
    }

    @Operation(
            summary = "오너 - 배송 주문 상태 변경 및 정보 등록",
            description = "오너가 배송에 필요한 정보들을 등록하고, 배송 상태를 SHIPPED로 바꿉니다."
    )
    @PatchMapping("/{photoLabId}/print-orders/{printOrderId}/shipping")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.PrintOrderStatusUpdated> registerShipping(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @PathVariable Long printOrderId,
            @RequestBody @Valid OwnerPhotoRequest.RegisterShipping request
    ) {
        OwnerPhotoResponse.PrintOrderStatusUpdated result =
                ownerPhotoCommandService.registerShipping(photoLabId, owner.memberId(), printOrderId, request);
        return ApiResponse.success(SuccessCode.OK, result);
    }

    @Operation(
            summary = "오너 - 현상 주문 상태 변경",
            description = "오너가 현상 주문의 상태를 원하는 값으로 변경합니다."
    )
    @PatchMapping("/{photoLabId}/print-orders/{printOrderId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OwnerPhotoResponse.PrintOrderStatusUpdated> updatePrintOrderStatus(
            @AuthenticationPrincipal AuthUser owner,
            @PathVariable Long photoLabId,
            @PathVariable Long printOrderId,
            @RequestBody @Valid OwnerPhotoRequest.UpdatePrintOrderStatus request
    ) {
        OwnerPhotoResponse.PrintOrderStatusUpdated result =
                ownerPhotoCommandService.updatePrintOrderStatus(photoLabId, owner.memberId(), printOrderId, request);
        return ApiResponse.success(SuccessCode.OK, result);
    }
}