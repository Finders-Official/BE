package com.finders.api.domain.photo.dto;

import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

import java.time.LocalDateTime;

public class PhotoResponse {

    @Builder
    public record MyDevelopmentOrder(
            Long developmentOrderId,

            // 현상소 정보
            Long photoLabId,
            String photoLabName,
            String photoLabAddress,

            // 주문 요약
            List<String> taskTypes,
            int rollCount,        //  " 2롤"
            int totalPrice,
            DevelopmentOrderStatus status,
            LocalDateTime createdAt,

            // 스캔 미리보기
            List<String> previewImageUrls  // 최대 3~4장
    ) {
        public static MyDevelopmentOrder from(
                DevelopmentOrder order,
                List<String> previewImageUrls
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
    public record ScanResult(
            Long scannedPhotoId,
            Integer displayOrder,
            String fileName,
            String imageKey,
            String signedUrl,
            Long expiresAt,
            LocalDateTime createdAt
    ) {
        public static ScanResult from(ScannedPhoto photo, String signedUrl, Long expiresAt) {
            return ScanResult.builder()
                    .scannedPhotoId(photo.getId())
                    .displayOrder(photo.getDisplayOrder())
                    .fileName(photo.getFileName())
                    .imageKey(photo.getImageKey())
                    .signedUrl(signedUrl)
                    .expiresAt(expiresAt)
                    .createdAt(photo.getCreatedAt())
                    .build();
        }
    }
}
