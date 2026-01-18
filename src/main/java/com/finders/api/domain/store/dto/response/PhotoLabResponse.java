package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

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

    // 커뮤니티 현상소 검색
    // 개별 항목 DTO
    @Builder
    public record PhotoLabSearchDTO(
            Long labId,
            String name,
            String address,
            String distance
    ) {
        public static PhotoLabSearchDTO from(PhotoLab photoLab, String distance, boolean locationAgreed) {
            return PhotoLabSearchDTO.builder()
                    .labId(photoLab.getId())
                    .name(photoLab.getName())
                    .address(photoLab.getAddress())
                    .distance(locationAgreed ? distance : null) // 위치 정보 미동의 시 null
                    .build();
        }
    }

    // 현상소 검색 리스트를 감싸는 DTO
    public record PhotoLabSearchListDTO(
            List<PhotoLabSearchDTO> photoLabSearchList
    ) {
        public static PhotoLabSearchListDTO from(List<PhotoLabSearchDTO> dtos) {
            return new PhotoLabSearchListDTO(dtos);
        }
    }
}
