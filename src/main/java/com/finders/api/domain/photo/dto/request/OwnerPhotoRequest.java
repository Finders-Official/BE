package com.finders.api.domain.photo.dto.request;

import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.enums.PrintOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;

import java.util.List;

public class OwnerPhotoRequest {

    /**
     * [오너] 스캔 업로드 presigned url 벌크 발급 요청
     */
    @Builder
    public record CreateScanUploadPresignedUrls(
            @Schema(example = "101", description = "현상 주문 ID (스캔 업로드 대상 주문)")
            @NotNull(message = "developmentOrderId는 필수입니다.")
            Long developmentOrderId,

            @Schema(example = "10", description = "발급 받을 presigned url 개수 (최소 1)")
            @Min(value = 1, message = "count는 1 이상이어야 합니다.")
            int count
    ) { }

    /**
     * [오너] 현상 주문 등록 요청
     *
     * - reservationId가 있으면 예약 기반 주문(예약 정보로 작업 유형/롤 수를 가져올 수 있음)
     * - reservationId가 없으면 memberId + taskTypes + rollCount 입력 필요
     */
    @Builder
    public record CreateDevelopmentOrder(
            @Schema(example = "55", description = "예약 ID (예약 기반 주문인 경우만 입력)")
            Long reservationId,

            @Schema(example = "2001", description = "회원 ID (예약이 없는 주문인 경우 필수)")
            Long memberId,

            @Schema(example = "24", description = "총 사진 수(컷 수). 0 이상")
            @Min(value = 0, message = "totalPhotos는 0 이상이어야 합니다.")
            int totalPhotos,

            @Schema(example = "15000", description = "총 결제 금액. 0 이상")
            @Min(value = 0, message = "totalPrice는 0 이상이어야 합니다.")
            int totalPrice,

            @Schema(example = "[\"DEVELOP\",\"SCAN\"]", description = "작업 유형 목록 (DEVELOP/SCAN/PRINT). 예약이 없는 주문이면 필수")
            List<String> taskTypes,

            @Schema(example = "2", description = "롤(필름) 개수. 예약이 없는 주문이면 필수")
            Integer rollCount,

            @Schema(
                    example = "2026-02-10T18:00:00",
                    description = "예상 작업 완료 시각, 현상 주문 완료 시, 완료된 시간으로 변경해서 매핑될 예정임"
            )
            LocalDateTime estimatedCompletedAt
    ) { }

    @Builder
    public record RegisterScannedPhotos(
            @Schema(description = "스캔 이미지 등록 리스트 (최소 1개)")
            @NotEmpty(message = "scannedPhotos는 최소 1개 이상이어야 합니다.")
            List<ScannedPhotoItem> scannedPhotos
    ) { }

    /**
     * [오너] 스캔 이미지 아이템
     */
    public record ScannedPhotoItem(
            @Schema(example = "scanned/1/orders/D11201011234/ab12cd34ef56...", description = "스토리지에 업로드된 이미지 키(object key)")
            @NotNull(message = "objectPath는 필수입니다.")
            String objectPath,

            @Schema(example = "IMG_0001.JPG", description = "원본 파일명(선택)")
            String fileName,

            @Schema(example = "1", description = "화면 표시 순서(정렬용). 0 또는 1부터 정책에 맞게 사용")
            @NotNull(message = "displayOrder는 필수입니다.")
            Integer displayOrder
    ) {
        public static ScannedPhotoItem of(String imageKey, String fileName, Integer displayOrder) {
            return new ScannedPhotoItem(imageKey, fileName, displayOrder);
        }
    }

    public record UpdateDevelopmentOrderStatus(
            @Schema(example = "SCANNING", description = "주문 상태 (RECEIVED/DEVELOPING/SCANNING/COMPLETED)")
            @NotNull(message = "status는 필수입니다.")
            DevelopmentOrderStatus status
    ) {}

    @Builder
    public record StartPrinting(
            @Schema(example = "2026-01-17T15:00:00", description = "예상 작업 완료 시간(estimatedAt)")
            @NotNull(message = "estimatedAt은 필수입니다.")
            LocalDateTime estimatedAt
    ) {}

    @Builder
    public record RegisterShipping(
            @Schema(example = "우체국택배", description = "택배사")
            @NotNull(message = "carrier는 필수입니다.")
            String carrier,

            @Schema(example = "123412341234", description = "송장번호")
            @NotNull(message = "trackingNumber는 필수입니다.")
            String trackingNumber,

            @Schema(example = "2026-01-17T18:30:00", description = "발송일시(없으면 now)")
            LocalDateTime shippedAt
    ) {}

    public record UpdatePrintOrderStatus(
            @Schema(example = "COMPLETED", description = "인화 주문 상태 변경")
            @NotNull(message = "status는 필수입니다.")
            PrintOrderStatus status
    ) {}
}
