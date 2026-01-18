package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.photo.dto.PhotoRequest;
import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.dto.PhotoResponse.ScanResult;
import org.springframework.data.domain.Slice;

public interface PhotoQueryService {

    PhotoResponse.MyCurrentWork getMyCurrentWork(Long memberId);

    Slice<PhotoResponse.MyDevelopmentOrder> getMyDevelopmentOrders(Long memberId, int page, int size);

    Slice<ScanResult> getMyScanResults(Long memberId, Long developmentOrderId, int page, int size);

    PhotoResponse.PrintOptions getPrintOptions();

    PhotoResponse.PrintQuote quote(Long memberId, PhotoRequest.PrintQuote request);

    PhotoResponse.PhotoLabAccount getPhotoLabAccount(Long memberId, Long developmentOrderId);
}
