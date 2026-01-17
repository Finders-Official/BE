package com.finders.api.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class MemberAddressRequest {

    @Builder
    public record Create(
            @NotBlank(message = "배송지 이름은 필수입니다.")
            @Size(max = 50, message = "배송지 이름은 50자 이내여야 합니다.")
            String addressName,

            @NotBlank(message = "우편번호는 필수입니다.")
            @Size(max = 10, message = "우편번호는 10자 이내여야 합니다.")
            String zipcode,

            @NotBlank(message = "주소는 필수입니다.")
            @Size(max = 200, message = "주소는 200자 이내여야 합니다.")
            String address,

            @Size(max = 100, message = "상세주소는 100자 이내여야 합니다.")
            String addressDetail,
            boolean isDefault
    ) {}
}
