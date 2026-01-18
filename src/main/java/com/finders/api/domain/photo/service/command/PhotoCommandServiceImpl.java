package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.PhotoRequest;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.PrintOrder;
import com.finders.api.domain.photo.entity.PrintOrderItem;
import com.finders.api.domain.photo.entity.PrintOrderPhoto;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.photo.repository.DevelopmentOrderRepository;
import com.finders.api.domain.photo.repository.PrintOrderItemRepository;
import com.finders.api.domain.photo.repository.PrintOrderPhotoRepository;
import com.finders.api.domain.photo.repository.PrintOrderRepository;
import com.finders.api.domain.photo.repository.ScannedPhotoRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoCommandServiceImpl implements PhotoCommandService {

    private final DevelopmentOrderRepository developmentOrderRepository;
    private final ScannedPhotoRepository scannedPhotoRepository;
    private final PrintOrderRepository printOrderRepository;
    private final PrintOrderItemRepository printOrderItemRepository;
    private final PrintOrderPhotoRepository printOrderPhotoRepository;

    @Override
    public Long skipPrint(Long memberId, Long developmentOrderId) {

        DevelopmentOrder devOrder = developmentOrderRepository.findById(developmentOrderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_ORDER_NOT_FOUND));

        if (!devOrder.getUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        devOrder.updateStatus(DevelopmentOrderStatus.COMPLETED);

        return devOrder.getId();
    }

    @Override
    public Long createPrintOrder(Long memberId, PhotoRequest.PrintQuote request) {

        // 1. 현상 주문 조회 + 소유자 검증
        DevelopmentOrder devOrder = developmentOrderRepository.findById(request.developmentOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "현상 주문을 찾을 수 없습니다."));

        if (!devOrder.getUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "주문 권한이 없습니다.");
        }

        // 2. 선택 사진 검증 (count 쿼리)
        List<Long> photoIds = request.photos().stream()
                .map(PhotoRequest.SelectedPhoto::scannedPhotoId)
                .toList();

        long validCount = scannedPhotoRepository.countAccessiblePhotos(
                memberId,
                devOrder.getId(),
                photoIds
        );

        if (validCount != photoIds.size()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 사진 중 유효하지 않은 항목이 있습니다.");
        }

        //  가격 계산 (quote 로직과 동일)
        int totalQuantity = request.photos().stream()
                .mapToInt(PhotoRequest.SelectedPhoto::quantity)
                .sum();

        int unitPrice =
                request.size().basePrice()
                        + request.filmType().extra(request.size().basePrice())
                        + request.printMethod().extraPrice()
                        + request.paperType().extraPrice()
                        + request.frameType().extraPrice();

        int printTotalPrice = unitPrice * totalQuantity;
        int deliveryFee = request.receiptMethod() == ReceiptMethod.DELIVERY ? 3000 : 0;
        int totalPrice = printTotalPrice + deliveryFee;

        // 4. PrintOrder 생성
        PrintOrder order = PrintOrder.create(
                devOrder,
                devOrder.getPhotoLab(),
                devOrder.getUser(),
                request.receiptMethod(),
                totalPrice
        );

        printOrderRepository.save(order);

        // 5.  PrintOrderItem (옵션 1세트)
        PrintOrderItem item = PrintOrderItem.create(
                order,
                request.filmType(),
                request.paperType(),
                request.printMethod(),
                request.size(),
                request.frameType(),
                unitPrice,
                totalPrice
        );
        printOrderItemRepository.save(item);

        // 6. PrintOrderPhoto (사진별 수량)
        for (PhotoRequest.SelectedPhoto p : request.photos()) {
            ScannedPhoto photo = scannedPhotoRepository.getReferenceById(p.scannedPhotoId());
            PrintOrderPhoto mapping =
                    PrintOrderPhoto.create(order, photo, p.quantity());
            printOrderPhotoRepository.save(mapping);
        }

        return order.getId();
    }

    @Override
    public Long confirmDepositReceipt(
            Long memberId,
            Long printOrderId,
            PhotoRequest.DepositReceiptConfirm request
    ) {
        PrintOrder printOrder = printOrderRepository
                .findByIdAndUserId(printOrderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_ORDER_NOT_FOUND));

        printOrder.confirmDepositReceipt(
                request.objectPath(),
                request.depositorName(),
                request.depositBankName()
        );

        return printOrder.getId();
    }
}


