package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.dto.response.MemberAddressResponse;

import java.util.List;

public interface MemberAddressQueryService {

    // 배송지 목록 조회
    List<MemberAddressResponse.AddressDetail> getMemberAddress(Long memberId);
}
