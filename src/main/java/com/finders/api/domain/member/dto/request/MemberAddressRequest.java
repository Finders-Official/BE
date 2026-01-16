package com.finders.api.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class MemberAddressRequest {

    @Builder
    public record Create(
            @NotBlank(message = "배송지 이름은 필수입니다.") String addressName,
            @NotBlank(message = "우편번호는 필수입니다.") String zipcode,
            @NotBlank(message = "주소는 필수입니다.") String address,
            String addressDetail,
            boolean isDefault
    ) {}
}
