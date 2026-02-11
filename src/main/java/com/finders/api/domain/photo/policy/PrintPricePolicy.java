package com.finders.api.domain.photo.policy;

import com.finders.api.domain.photo.dto.request.PhotoRequest;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PrintPricePolicy {

    private static final int DELIVERY_FEE = 3000;
    private static final int DEFAULT_BASE_PRICE = 1400;

    public PriceResult calculate(PhotoRequest.PrintQuote request) {

        if (request == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "요청이 올바르지 않습니다.");
        }
        if (request.photos() == null || request.photos().isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "인화할 사진은 최소 1장 이상 선택해야 합니다.");
        }

        int totalQuantity = request.photos().stream()
                .mapToInt(p -> {
                    if (p == null || p.quantity() < 1) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "사진 수량(quantity)은 1 이상이어야 합니다.");
                    }
                    return p.quantity();
                })
                .sum();

        // size가 null일 수 있는 케이스(quote 단계) 대비
        int basePrice = (request.size() != null)
                ? request.size().basePrice()
                : DEFAULT_BASE_PRICE;

        int filmExtra = (request.filmType() != null)
                ? request.filmType().extra(basePrice)
                : 0;

        int methodExtra = (request.printMethod() != null)
                ? request.printMethod().extraPrice()
                : 0;

        int paperExtra = (request.paperType() != null)
                ? request.paperType().extraPrice()
                : 0;

        int frameExtra = (request.frameType() != null)
                ? request.frameType().extraPrice()
                : 0;

        int unitPrice = basePrice + filmExtra + methodExtra + paperExtra + frameExtra;

        int printTotalPrice = unitPrice * totalQuantity;

        int deliveryFee = (request.receiptMethod() == ReceiptMethod.DELIVERY)
                ? DELIVERY_FEE
                : 0;

        int orderTotalPrice = printTotalPrice + deliveryFee;

        return new PriceResult(
                unitPrice,
                printTotalPrice,
                deliveryFee,
                orderTotalPrice,
                totalQuantity
        );
    }

    public int deliveryFee() {
        return DELIVERY_FEE;
    }

    public record PriceResult(
            int unitPrice,
            int printTotalPrice,
            int deliveryFee,
            int orderTotalPrice,
            int totalQuantity
    ) {}
}
