package com.finders.api.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class PhotoLabRequest {
    public record Create(
            @NotNull(message = "오너 ID는 필수입니다.")
            Long ownerId,

            @NotBlank(message = "현상소 이름은 필수입니다.")
            @Size(max = 80, message = "현상소 이름은 80자 이하여야 합니다.")
            String name,

            @NotBlank(message = "주소는 필수입니다.")
            @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
            String address,

            @Size(max = 100, message = "상세 주소는 100자 이하여야 합니다.")
            String addressDetail,

            @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
            String zipcode,

            @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
            String phone,

            @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
            String description,

            @NotNull(message = "위도는 필수입니다.")
            Double latitude,

            @NotNull(message = "경도는 필수입니다.")
            Double longitude,

            Boolean isDeliveryAvailable,

            @Min(value = 1, message = "시간당 최대 예약 수는 1 이상이어야 합니다.")
            Integer maxReservationsPerHour
    ) {
    }
}
