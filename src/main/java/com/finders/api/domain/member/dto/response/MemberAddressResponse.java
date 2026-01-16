package com.finders.api.domain.member.dto.response;

import com.finders.api.domain.member.entity.MemberAddress;
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
    ) {
        public static AddressDetail from(MemberAddress address) {
            return AddressDetail.builder()
                    .addressId(address.getId())
                    .addressName(address.getAddressName())
                    .zipcode(address.getZipcode())
                    .address(address.getAddress())
                    .addressDetail(address.getAddressDetail())
                    .isDefault(address.isDefault())
                    .build();
        }
    }
}
