package com.finders.api.domain.member.service.query;

import com.finders.api.domain.member.dto.response.MemberAddressResponse;
import com.finders.api.domain.member.repository.MemberAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberAddressQueryServiceImpl implements MemberAddressQueryService {

    private final MemberAddressRepository memberAddressRepository;

    @Override
    public List<MemberAddressResponse.AddressDetail> getMemberAddress(Long memberId) {
        return memberAddressRepository.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(memberId).stream()
                .map(address -> MemberAddressResponse.AddressDetail.builder()
                        .addressId(address.getId())
                        .addressName(address.getAddressName())
                        .zipcode(address.getZipcode())
                        .address(address.getAddress())
                        .addressDetail(address.getAddressDetail())
                        .isDefault(address.isDefault())
                        .build())
                .toList();
    }
}
