package com.finders.api.domain.photo.dto;


import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import java.util.List;

public class OwnerPhotoResponse {

    /**
     * [오너] 주문 등록 성공 응답
     */
    public record Created(
            Long developmentOrderId
    ) {
        public static Created of(Long developmentOrderId) {
            return new Created(developmentOrderId);
        }
    }

    /**
     * [오너] 스캔 업로드 presigned url 벌크 발급 응답
     */
    public record PresignedUrls(
            Long photoLabId,
            Long developmentOrderId,
            String orderCode,
            Long expiresAtEpochSecond,
            List<Item> items
    ) {
        public static PresignedUrls of(Long photoLabId, Long developmentOrderId, String orderCode,
                                       Long expiresAtEpochSecond, List<Item> items) {
            return new PresignedUrls(photoLabId, developmentOrderId, orderCode, expiresAtEpochSecond, items);
        }
    }

    public record Item(
            Integer displayOrder,
            String imageKey,
            String signedUrl
    ) {
        public static Item of(Integer displayOrder, String imageKey, String signedUrl) {
            return new Item(displayOrder, imageKey, signedUrl);
        }
    }

    /**
     * [오너] (4) 스캔 이미지 DB 등록 결과
     */
    public record ScannedPhotosRegistered(
            Long developmentOrderId,
            Integer savedCount
    ) {
        public static ScannedPhotosRegistered of(Long developmentOrderId, Integer savedCount) {
            return new ScannedPhotosRegistered(developmentOrderId, savedCount);
        }
    }

    public record DevelopmentOrderStatusUpdated(
            Long developmentOrderId,
            DevelopmentOrderStatus status
    ) {
        public static DevelopmentOrderStatusUpdated of(Long id, DevelopmentOrderStatus status) {
            return new DevelopmentOrderStatusUpdated(id, status);
        }
    }
}
