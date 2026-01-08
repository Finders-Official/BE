package com.finders.api.domain.store.dto;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class PhotoLabResponse {

    @Builder
    public record Create(
            Long id,
            Long ownerId,
            String name,
            String address,
            String addressDetail,
            String zipcode,
            String phone,
            String description,
            Double latitude,
            Double longitude,
            PhotoLabStatus status,
            Boolean isDeliveryAvailable,
            Integer maxReservationsPerHour,
            LocalDateTime createdAt
    ) {
        public static Create from(PhotoLab photoLab) {
            return Create.builder()
                    .id(photoLab.getId())
                    .ownerId(photoLab.getOwner().getId())
                    .name(photoLab.getName())
                    .address(photoLab.getAddress())
                    .addressDetail(photoLab.getAddressDetail())
                    .zipcode(photoLab.getZipcode())
                    .phone(photoLab.getPhone())
                    .description(photoLab.getDescription())
                    .latitude(photoLab.getLatitude() != null ? photoLab.getLatitude().doubleValue() : null)
                    .longitude(photoLab.getLongitude() != null ? photoLab.getLongitude().doubleValue() : null)
                    .status(photoLab.getStatus())
                    .isDeliveryAvailable(photoLab.isDeliveryAvailable())
                    .maxReservationsPerHour(photoLab.getMaxReservationsPerHour())
                    .createdAt(photoLab.getCreatedAt())
                    .build();
        }
    }
}
