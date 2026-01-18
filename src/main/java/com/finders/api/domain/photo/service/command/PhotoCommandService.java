package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.PhotoRequest;

public interface PhotoCommandService {
    Long createPrintOrder(Long memberId, PhotoRequest.PrintQuote request);

    Long confirmDepositReceipt(Long memberId, Long printOrderId, PhotoRequest.DepositReceiptConfirm request);

    Long skipPrint(Long memberId, Long developmentOrderId);
}
