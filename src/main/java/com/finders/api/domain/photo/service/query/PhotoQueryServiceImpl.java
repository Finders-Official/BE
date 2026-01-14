package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.repository.DevelopmentOrderRepository;
import com.finders.api.domain.photo.repository.ScannedPhotoRepository;
import com.finders.api.domain.photo.repository.projection.ScanPreviewProjection;
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

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<DevelopmentOrder> orderPage =
                developmentOrderRepository.findMyOrdersWithPhotoLab(memberId, pageable);

        if (orderPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Long> orderIds = orderPage.getContent().stream()
                .map(DevelopmentOrder::getId)
                .toList();

        // (1) DB에서 주문당 PREVIEW_LIMIT개만 가져오기
        List<ScanPreviewProjection> previews =
                scannedPhotoRepository.findPreviewByOrderIds(orderIds, PREVIEW_LIMIT);

        // (2) imageKey만 모아서 batch signedUrl 생성
        List<String> keys = previews.stream()
                .map(ScanPreviewProjection::getImageKey)
                .toList();

        Map<String, StorageResponse.SignedUrl> signedMap =
                storageService.getSignedUrls(keys, null);

        // orderId -> urls
        Map<Long, List<String>> previewUrlMap = new HashMap<>();
        for (ScanPreviewProjection p : previews) {
            StorageResponse.SignedUrl signed = signedMap.get(p.getImageKey());
            if (signed == null) continue;

            previewUrlMap.computeIfAbsent(p.getOrderId(), k -> new ArrayList<>())
                    .add(signed.url());
        }

        List<PhotoResponse.MyDevelopmentOrder> dtoList = orderPage.getContent().stream()
                .map(order -> PhotoResponse.MyDevelopmentOrder.from(
                        order,
                        previewUrlMap.getOrDefault(order.getId(), List.of())
                ))
                .toList();

        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }


    @Override
    public Slice<PhotoResponse.ScanResult> getMyScanResults(Long memberId, Long developmentOrderId, int page, int size) {

        boolean isMine = developmentOrderRepository.existsByIdAndUser_Id(developmentOrderId, memberId);
        if (!isMine) throw new CustomException(ErrorCode.FORBIDDEN, "해당 주문에 접근 권한이 없습니다.");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));

        Slice<ScannedPhoto> slice =
                scannedPhotoRepository.findByOrderIdOrderByDisplayOrderAsc(developmentOrderId, pageable);

        List<String> keys = slice.getContent().stream()
                .map(ScannedPhoto::getImageKey)
                .toList();

        Map<String, StorageResponse.SignedUrl> signedMap =
                storageService.getSignedUrls(keys, null);

        List<PhotoResponse.ScanResult> content = slice.getContent().stream()
                .map(photo -> {
                    StorageResponse.SignedUrl signed = signedMap.get(photo.getImageKey());
                    if (signed == null) {
                        // 사진이 없는 경우
                        return PhotoResponse.ScanResult.from(photo, null, null);
                    }
                    return PhotoResponse.ScanResult.from(photo, signed.url(), signed.expiresAtEpochSecond());
                })
                .toList();

        return new SliceImpl<>(content, pageable, slice.hasNext());
    }

}