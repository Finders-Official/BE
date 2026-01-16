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
        return memberAddressRepository.findAllByUser_IdOrderByIsDefaultDescCreatedAtDesc(memberId).stream()
                .map(MemberAddressResponse.AddressDetail::from)
                .toList();
    }
}
