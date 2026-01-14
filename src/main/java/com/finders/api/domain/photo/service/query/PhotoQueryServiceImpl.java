package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.repository.DevelopmentOrderRepository;
import com.finders.api.domain.photo.repository.ScannedPhotoRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoQueryServiceImpl implements PhotoQueryService {

    private static final int PREVIEW_LIMIT = 3;

    private final DevelopmentOrderRepository developmentOrderRepository;
    private final ScannedPhotoRepository scannedPhotoRepository;
    private final StorageService storageService;

    @Override
    public Page<PhotoResponse.MyDevelopmentOrder> getMyDevelopmentOrders(Long memberId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<DevelopmentOrder> orderPage =
                developmentOrderRepository.findMyOrdersWithPhotoLab(memberId, pageable);

        if (orderPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Long> orderIds = orderPage.getContent().stream()
                .map(DevelopmentOrder::getId)
                .toList();

        List<ScannedPhoto> scannedPhotos =
                scannedPhotoRepository.findByOrderIdInOrderByDisplayOrderAsc(orderIds);

        Map<Long, List<String>> previewUrlMap = new HashMap<>();

        for (ScannedPhoto sp : scannedPhotos) {
            Long orderId = sp.getOrder().getId();

            List<String> urls = previewUrlMap.computeIfAbsent(orderId, k -> new ArrayList<>());
            if (urls.size() >= PREVIEW_LIMIT)
                continue;

            String imageKey = sp.getImageKey();
            String signedUrl = storageService.getSignedUrl(imageKey, null).url();

            urls.add(signedUrl);
        }

        List<PhotoResponse.MyDevelopmentOrder> dtoList = orderPage.getContent().stream()
                .map(order -> PhotoResponse.MyDevelopmentOrder.from(
                        order,
                        previewUrlMap.getOrDefault(order.getId(), List.of())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }

    @Override
    public Slice<PhotoResponse.ScanResult> getMyScanResults(Long memberId, Long developmentOrderId, int page,
                                                            int size) {

        boolean isMine = developmentOrderRepository.existsByIdAndUser_Id(developmentOrderId, memberId);

        if (!isMine) throw new CustomException(ErrorCode.FORBIDDEN, "해당 주문에 접근 권한이 없습니다.");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));

        Slice<ScannedPhoto> slice = scannedPhotoRepository.findByOrderIdOrderByDisplayOrderAsc(developmentOrderId,
                pageable);

        return new SliceImpl<>(
                slice.getContent().stream()
                        .map(photo -> {
                            StorageResponse.SignedUrl signed = storageService.getSignedUrl(photo.getImageKey(), null);
                            return PhotoResponse.ScanResult.from(photo, signed.url(), signed.expiresAtEpochSecond());
                        })
                        .collect(Collectors.toList()),
                pageable,
                slice.hasNext()
        );
    }
}