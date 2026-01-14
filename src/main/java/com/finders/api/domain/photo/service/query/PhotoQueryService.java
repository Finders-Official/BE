package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.dto.PhotoResponse.ScanResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public interface PhotoQueryService {
    Page<PhotoResponse.MyDevelopmentOrder> getMyDevelopmentOrders(Long memberId, int page, int size);

    Slice<ScanResult> getMyScanResults(Long memberId, Long developmentOrderId, int page, int size);
}
