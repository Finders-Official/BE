package com.finders.api.domain.member.service.command;

import com.finders.api.domain.member.dto.request.MemberAddressRequest;
import com.finders.api.domain.member.dto.response.MemberAddressResponse;

public interface MemberAddressCommandService {

    // 새로운 배송지 추가
    MemberAddressResponse.AddressDetail createAddress(Long memberId, MemberAddressRequest.Create request);
}
