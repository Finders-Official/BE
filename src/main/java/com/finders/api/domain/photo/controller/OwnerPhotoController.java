package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.OwnerPhotoRequest;
import com.finders.api.domain.photo.dto.OwnerPhotoResponse;
import com.finders.api.domain.photo.service.command.OwnerPhotoCommandService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Owner Photo", description = "오너용 현상/스캔 등록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/photo-labs")
public class OwnerPhotoController {

    private final OwnerPhotoCommandService ownerPhotoCommandService;

    // TODO: JWT 인증 적용 시 @AuthenticationPrincipal로 교체
    private static final Long TEMP_OWNER_ID = 1L;

    @Operation(
            summary = "오너 - 스캔 업로드 presigned url 벌크 발급(PUT)",
            description = "private 버킷에 PUT 업로드 가능한 presigned url을 count 만큼 발급합니다."
    )
    @PostMapping("/{photoLabId}/scan-uploads/presigned-urls")
    public ApiResponse<OwnerPhotoResponse.PresignedUrls> createScanUploadPresignedUrls(
            @PathVariable Long photoLabId,
            @RequestBody @Valid OwnerPhotoRequest.CreateScanUploadPresignedUrls request
    ) {
        OwnerPhotoResponse.PresignedUrls response = ownerPhotoCommandService.createScanUploadPresignedUrls(
                photoLabId,
                TEMP_OWNER_ID,
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
    public ApiResponse<OwnerPhotoResponse.Created> createDevelopmentOrder(
            @PathVariable Long photoLabId,
            @RequestBody @Valid OwnerPhotoRequest.CreateDevelopmentOrder request
    ) {
        Long orderId = ownerPhotoCommandService.createDevelopmentOrder(
                photoLabId,
                TEMP_OWNER_ID,
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
    public ApiResponse<OwnerPhotoResponse.ScannedPhotosRegistered> registerScannedPhotos(
            @PathVariable Long photoLabId,
            @PathVariable Long developmentOrderId,
            @RequestBody @Valid OwnerPhotoRequest.RegisterScannedPhotos request
    ) {
        OwnerPhotoResponse.ScannedPhotosRegistered response =
                ownerPhotoCommandService.registerScannedPhotos(
                        photoLabId,
                        TEMP_OWNER_ID,
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
    public ApiResponse<OwnerPhotoResponse.DevelopmentOrderStatusUpdated> updateDevelopmentOrderStatus(
            @PathVariable Long photoLabId,
            @PathVariable Long developmentOrderId,
            @RequestBody @Valid OwnerPhotoRequest.UpdateDevelopmentOrderStatus request
    ) {
        OwnerPhotoResponse.DevelopmentOrderStatusUpdated response =
                ownerPhotoCommandService.updateDevelopmentOrderStatus(
                        photoLabId,
                        TEMP_OWNER_ID,
                        developmentOrderId,
                        request
                );

        return ApiResponse.success(SuccessCode.OK, response);
    }

}
