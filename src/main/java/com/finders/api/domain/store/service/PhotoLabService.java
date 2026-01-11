package com.finders.api.domain.store.service;

import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.member.repository.MemberOwnerRepository;
import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabService {

    private final PhotoLabRepository photoLabRepository;
    private final MemberOwnerRepository memberOwnerRepository;

    @Transactional
    public PhotoLabResponse.Create createPhotoLab(Long ownerId, PhotoLabRequest.Create request) {
        log.info("[PhotoLabService.createPhotoLab] ownerId: {}", ownerId);

        MemberOwner owner = memberOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        PhotoLab photoLab = PhotoLab.builder()
                .owner(owner)
                .name(request.name())
                .description(request.description())
                .phone(request.phone())
                .zipcode(request.zipcode())
                .address(request.address())
                .addressDetail(request.addressDetail())
                .latitude(BigDecimal.valueOf(request.latitude()))
                .longitude(BigDecimal.valueOf(request.longitude()))
                .isDeliveryAvailable(request.isDeliveryAvailable())
                .maxReservationsPerHour(request.maxReservationsPerHour())
                .build();

        photoLabRepository.save(photoLab);
        return PhotoLabResponse.Create.from(photoLab);
    }
}

