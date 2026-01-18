package com.finders.api.domain.member.service.command;

import com.finders.api.domain.member.dto.request.MemberAddressRequest;
import com.finders.api.domain.member.dto.response.MemberAddressResponse;
import com.finders.api.domain.member.entity.MemberAddress;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberAddressRepository;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberAddressCommandServiceImpl implements MemberAddressCommandService {

    private final MemberAddressRepository memberAddressRepository;
    private final MemberUserRepository memberUserRepository;

    @Override
    public MemberAddressResponse.AddressDetail createAddress(Long memberId, MemberAddressRequest.Create request) {
        MemberUser user = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 첫 번째 배송지인지 확인
        boolean isFirstAddress = !memberAddressRepository.existsByUserId(memberId);

        // 요청에서 기본 배송지로 설정했거나, 첫 번째 배송지라면 true
        boolean shouldBeDefault = request.isDefault() || isFirstAddress;

        // 이번 배송지가 기본 배송지가 되어야 한다면, 기존 기본 배송지는 해제
        if (shouldBeDefault) {
            memberAddressRepository.findByUser_IdAndIsDefaultTrue(memberId)
                    .ifPresent(existingDefault -> existingDefault.updateIsDefault(false));
        }

        MemberAddress newAddress = MemberAddress.builder()
                .user(user)
                .addressName(request.addressName())
                .zipcode(request.zipcode())
                .address(request.address())
                .addressDetail(request.addressDetail())
                .isDefault(request.isDefault())
                .build();

        MemberAddress savedAddress = memberAddressRepository.save(newAddress);

        return MemberAddressResponse.AddressDetail.from(savedAddress);
    }
}
