package com.finders.api.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class MemberAddressRequest {

    @Builder
    public record Create(
            @Schema(description = "배송지 이름", example = "우리집")
            @NotBlank(message = "배송지 이름은 필수입니다.")
            @Size(max = 50, message = "배송지 이름은 50자 이내여야 합니다.")
            String addressName,

            @Schema(description = "우편번호", example = "06035")
            @NotBlank(message = "우편번호는 필수입니다.")
            @Size(max = 10, message = "우편번호는 10자 이내여야 합니다.")
            String zipcode,

            @Schema(description = "주소", example = "서울특별시 강남구 가로수길 5")
            @NotBlank(message = "주소는 필수입니다.")
            @Size(max = 200, message = "주소는 200자 이내여야 합니다.")
            String address,

            @Schema(description = "상세주소", example = "101동 202호")
            @Size(max = 100, message = "상세주소는 100자 이내여야 합니다.")
            String addressDetail,

            @Schema(description = "기본 배송지 여부", example = "true")
            boolean isDefault
    ) {}
}
