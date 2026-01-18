package com.finders.api.domain.member.dto.response;

import com.finders.api.domain.member.entity.MemberAddress;
import lombok.Builder;

import java.time.LocalDateTime;

public class MemberAddressResponse {

    @Builder
    public record AddressDetail(
            Long addressId,
            String addressName,
            String zipcode,
            String address,
            String addressDetail,
            boolean isDefault,
            LocalDateTime createdAt
    ) {
        public static AddressDetail from(MemberAddress address) {
            return AddressDetail.builder()
                    .addressId(address.getId())
                    .addressName(address.getAddressName())
                    .zipcode(address.getZipcode())
                    .address(address.getAddress())
                    .addressDetail(address.getAddressDetail())
                    .isDefault(address.isDefault())
                    .createdAt(address.getCreatedAt())
                    .build();
        }
    }
}
