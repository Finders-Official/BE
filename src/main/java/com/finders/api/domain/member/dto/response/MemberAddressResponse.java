package com.finders.api.domain.member.dto.response;

import lombok.Builder;

public class MemberAddressResponse {

    @Builder
    public record AddressDetail(
            Long addressId,
            String addressName,
            String zipcode,
            String address,
            String addressDetail,
            boolean isDefault
    ) {}
}
