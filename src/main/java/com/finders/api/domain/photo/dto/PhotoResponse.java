package com.finders.api.domain.photo.dto;

import com.finders.api.domain.photo.entity.Delivery;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.PrintOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

import java.time.LocalDateTime;

public class PhotoResponse {

    @Builder
    @Schema(name = "MyCurrentWorkResponse", description = "회원 - 내 진행중 작업 응답")
    public record MyCurrentWork(

            @Schema(description = "현상 주문 ID", example = "101")
            Long developmentOrderId,

            @Schema(description = "현상소 ID", example = "10")
            Long photoLabId,

            @Schema(description = "현상소 이름", example = "파인더스 상도점")
            String photoLabName,

            @Schema(description = "현상 주문 상태", example = "SCANNING")
            DevelopmentOrderStatus developmentStatus,

            @Schema(description = "주문 생성일시", example = "2026-01-16T15:10:00")
            LocalDateTime createdAt,

            @Schema(description = "현상 완료 일시", example = "2026-01-16T15:10:00")
            LocalDateTime completedAt,


            @Schema(description = "인화 진행 정보(인화 없으면 null)")
            PrintProgress print,

            @Schema(description = "배송 진행 정보(배송 아닐 경우 null)")
            DeliveryProgress delivery
    ) {
        public static MyCurrentWork from(
                DevelopmentOrder order,
                PrintProgress print,
                DeliveryProgress delivery
        ) {
            return MyCurrentWork.builder()
                    .developmentOrderId(order.getId())
                    .photoLabId(order.getPhotoLab().getId())
                    .photoLabName(order.getPhotoLab().getName())
                    .developmentStatus(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .completedAt(order.getCompletedAt())
                    .print(print)
                    .delivery(delivery)
                    .build();
        }
    }

    @Builder
    @Schema(name = "PrintProgressResponse", description = "회원 - 인화 진행 상태")
    public record PrintProgress(

            @Schema(description = "인화 주문 ID", example = "201")
            Long printOrderId,

            @Schema(description = "인화 주문 상태", example = "PRINTING")
            com.finders.api.domain.photo.enums.PrintOrderStatus status,

            @Schema(description = "수령 방식(PICKUP/DELIVERY)", example = "DELIVERY")
            com.finders.api.domain.photo.enums.ReceiptMethod receiptMethod,

            @Schema(description = "예상 완료 시각", example = "2026-01-17T14:00:00")
            LocalDateTime estimatedAt,

            @Schema(description = "인화 완료 시각", example = "2026-01-17T13:30:00")
            LocalDateTime completedAt
    ) {
        public static PrintProgress from(PrintOrder po) {
            return new PrintProgress(
                    po.getId(),
                    po.getStatus(),
                    po.getReceiptMethod(),
                    po.getEstimatedAt(),
                    po.getCompletedAt()
            );
        }
    }

    @Builder
    @Schema(name = "DeliveryProgressResponse", description = "회원 - 배송 진행 상태")
    public record DeliveryProgress(

            @Schema(description = "배송 ID", example = "301")
            Long deliveryId,

            @Schema(description = "배송 상태", example = "SHIPPED")
            com.finders.api.domain.photo.enums.DeliveryStatus status,

            @Schema(description = "받는 사람", example = "이승주")
            String recipient,

            @Schema(description = "수령인 연락처", example = "010-1234-5678")
            String recipientNumber,

            @Schema(description = "수령인 주소", example = "서울특별시 강남구")
            String recipientAddress,

            @Schema(description = "수령인 상세 주소", example = "역삼동 123-123")
            String AddressDetail,

            @Schema(description = "보낸 사람 이름", example = "예술사진관 사장")
            String sender,

            @Schema(description = "택배사", example = "CJ대한통운")
            String carrier,

            @Schema(description = "송장번호", example = "123412341234")
            String trackingNumber,

            @Schema(description = "발송 시각", example = "2026-01-17T15:00:00")
            LocalDateTime shippedAt,

            @Schema(description = "도착 시각", example = "2026-01-18T12:00:00")
            LocalDateTime deliveredAt


    ) {
        public static DeliveryProgress from(Delivery d) {
            return new DeliveryProgress(
                    d.getId(),
                    d.getStatus(),
                    d.getRecipientName(),
                    d.getPrintOrder().getPhotoLab().getName(),
                    d.getRecipientName(),
                    d.getAddress(),
                    d.getAddressDetail(),
                    d.getCarrier(),
                    d.getTrackingNumber(),
                    d.getShippedAt(),
                    d.getDeliveredAt()
            );
        }
    }

    @Builder
    @Schema(name = "MyDevelopmentOrderResponse", description = "회원 - 내 지난 작업(현상 주문) 요약 응답")
    public record MyDevelopmentOrder(
            @Schema(description = "현상 주문 ID", example = "101")
            Long developmentOrderId,

            @Schema(description = "현상소 ID", example = "10")
            Long photoLabId,
            @Schema(description = "현상소 이름", example = "파인더스 현상소")
            String photoLabName,
            @Schema(description = "현상소 주소", example = "서울특별시 강남구 ...")
            String photoLabAddress,

            @Schema(description = "요청 작업 유형 목록(DEVELOP/SCAN/PRINT)", example = "[\"DEVELOP\",\"SCAN\"]")
            List<String> taskTypes,
            @Schema(description = "필름 롤 수", example = "2")
            int rollCount,
            @Schema(description = "주문 총액(현상/스캔/인화 포함)", example = "24000")
            int totalPrice,
            @Schema(description = "주문 상태", example = "RECEIVED")
            DevelopmentOrderStatus status,
            @Schema(description = "주문 생성일시", example = "2026-01-16T15:10:00")
            LocalDateTime createdAt,

            @Schema(description = "스캔 미리보기 이미지 URL(최대 3~4장)", example = "[\"https://...\",\"https://...\"]")
            List<String> previewImageUrls,

             @Schema(description = "인화 배송지(없으면 null)", example = "서울특별시 강남구 ...")
                    String deliveryAddress,

            @Schema(description = "인화 배송 상세주소(없으면 null)", example = "101동 1203호")
            String deliveryAddressDetail,

            @Schema(description = "배송 완료 일시(없으면 null)", example = "2026-01-20T18:10:00")
            LocalDateTime deliveredAt
    ) {
        public static MyDevelopmentOrder from(
                DevelopmentOrder order,
                List<String> previewImageUrls,
                 Delivery delivery
        ) {
            return MyDevelopmentOrder.builder()
                    .developmentOrderId(order.getId())
                    .photoLabId(order.getPhotoLab().getId())
                    .photoLabName(order.getPhotoLab().getName())
                    .photoLabAddress(order.getPhotoLab().getAddress())
                    .taskTypes(extractTaskTypes(order))
                    .rollCount(order.getRollCount())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .previewImageUrls(previewImageUrls)
                    .deliveryAddress(delivery != null ? delivery.getAddress() : null)
                    .deliveryAddressDetail(delivery != null ? delivery.getAddressDetail() : null)
                    .deliveredAt(delivery != null ? delivery.getDeliveredAt() : null)
                    .build();
        }

        private static List<String> extractTaskTypes(DevelopmentOrder order) {
            List<String> result = new ArrayList<>(3);
            if (order.isDevelop()) result.add("DEVELOP");
            if (order.isScan()) result.add("SCAN");
            if (order.isPrint()) result.add("PRINT");
            return result;
        }
    }

    @Builder
    @Schema(name = "ScanResultResponse", description = "회원 - 스캔 결과 사진 항목 응답")
    public record ScanResult(
            @Schema(description = "스캔 사진 ID", example = "501")
            Long scannedPhotoId,
            @Schema(description = "표시 순서", example = "1")
            Integer displayOrder,
            @Schema(description = "원본 파일명", example = "IMG_0001.jpg")
            String fileName,
            @Schema(description = "저장 키(S3/GCS object key)", example = "scan/orders/101/IMG_0001.jpg")
            String objectPath,
            @Schema(description = "서명된 다운로드 URL", example = "https://storage.googleapis.com/...signature=...")
            String signedUrl,
            @Schema(description = "signedUrl 만료 epoch(ms)", example = "1768557000000")
            Long expiresAt,
            @Schema(description = "생성일시", example = "2026-01-16T15:10:00")
            LocalDateTime createdAt
    ) {
        public static ScanResult from(ScannedPhoto photo, String signedUrl, Long expiresAt) {
            return ScanResult.builder()
                    .scannedPhotoId(photo.getId())
                    .displayOrder(photo.getDisplayOrder())
                    .fileName(photo.getFileName())
                    .objectPath(photo.getObjectPath())
                    .signedUrl(signedUrl)
                    .expiresAt(expiresAt)
                    .createdAt(photo.getCreatedAt())
                    .build();
        }
    }

    /* =========================================================
       인화: 옵션 목록 응답 (/print/options)
       - 모든 옵션 필수 선택이므로 단일 타입(PrintOptionItem)으로 통일
       - size는 basePrice 사용, 나머지는 extraPrice 또는 rate 사용
     ========================================================= */

    @Builder
    @Schema(name = "PrintOptionItem", description = "인화 옵션 항목(공통 타입)")
    public record PrintOptionItem(
            @Schema(description = "옵션 코드(enum name)", example = "SIZE_6x8")
            String code,
            @Schema(description = "옵션 표시명", example = "6*8")
            String label,

            @Schema(
                    description = "기본금(사이즈 옵션에서만 사용). size가 아니면 null",
                    example = "2600",
                    nullable = true
            )
            Integer basePrice,

            @Schema(
                    description = "정액 추가금(정액 옵션에서 사용). 해당 없으면 0 또는 null",
                    example = "1000",
                    nullable = true
            )
            Integer extraPrice,

            @Schema(
                    description = "비율 추가금(필름 옵션 등). 예: 0.1",
                    example = "0.1",
                    nullable = true
            )
            Double rate,

            @Schema(
                    description = "비율 옵션의 절사/반올림 정책. 예: FLOOR_100(백원단위 버림)",
                    example = "FLOOR_100",
                    nullable = true
            )
            String roundingPolicy
    ) {
        public static PrintOptionItem size(String code, String label, int basePrice) {
            return PrintOptionItem.builder()
                    .code(code)
                    .label(label)
                    .basePrice(basePrice)
                    .extraPrice(null)
                    .rate(null)
                    .roundingPolicy(null)
                    .build();
        }

        public static PrintOptionItem flat(String code, String label, int extraPrice) {
            return PrintOptionItem.builder()
                    .code(code)
                    .label(label)
                    .basePrice(null)
                    .extraPrice(extraPrice)
                    .rate(null)
                    .roundingPolicy(null)
                    .build();
        }

        public static PrintOptionItem rate(String code, String label, double rate, String roundingPolicy) {
            return PrintOptionItem.builder()
                    .code(code)
                    .label(label)
                    .basePrice(null)
                    .extraPrice(0)
                    .rate(rate)
                    .roundingPolicy(roundingPolicy)
                    .build();
        }
    }

    @Builder
    @Schema(name = "PrintOptionsResponse", description = "회원 - 인화 옵션 목록 조회 응답(/print/options)")
    public record PrintOptions(
            @Schema(description = "배송비(수령방식 DELIVERY일 때 적용)", example = "3000")
            int deliveryFee,

            @Schema(description = "필름 종류 옵션 목록(비율 추가금 포함)")
            List<PrintOptionItem> filmTypes,

            @Schema(description = "인화 방식 옵션 목록(정액 추가금)")
            List<PrintOptionItem> printMethods,

            @Schema(description = "인화지 옵션 목록(정액 추가금)")
            List<PrintOptionItem> paperTypes,

            @Schema(description = "사이즈 옵션 목록(기본금 basePrice)")
            List<PrintOptionItem> sizes,

            @Schema(description = "인화 유형(프레임) 옵션 목록(정액 추가금)")
            List<PrintOptionItem> frameTypes
    ) { }

    @Builder
    @Schema(name = "PrintQuoteResponse", description = "회원 - 인화 실시간 견적 응답(/print/quote)")
    public record PrintQuote(

            @Schema(description = "인화 금액(배송비 제외)", example = "11550")
            int printAmount,

            @Schema(description = "배송비(PICKUP이면 0)", example = "3000")
            int deliveryFee,

            @Schema(description = "최종 결제 금액", example = "14550")
            int totalAmount
    ) { }

    @Builder
    @Schema(name = "PhotoLabAccount", description = "회원 - 현상소 인화 계좌 정보 조회")
    public record PhotoLabAccount(
            String bankName,
            String bankAccountNumber,
            String bankAccountHolder
    ) {}
}
