package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.PhotoRequest;
import com.finders.api.domain.photo.entity.Delivery;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.PrintOrder;
import com.finders.api.domain.photo.entity.PrintOrderItem;
import com.finders.api.domain.photo.entity.PrintOrderPhoto;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.photo.policy.PrintPricePolicy;
import com.finders.api.domain.photo.repository.DeliveryRepository;
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

    private final PrintPricePolicy printPricePolicy;

    private final DevelopmentOrderRepository developmentOrderRepository;
    private final ScannedPhotoRepository scannedPhotoRepository;
    private final PrintOrderRepository printOrderRepository;
    private final PrintOrderItemRepository printOrderItemRepository;
    private final PrintOrderPhotoRepository printOrderPhotoRepository;
    private final DeliveryRepository deliveryRepository;

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

        // 1) 현상 주문 조회 + 소유자 검증
        DevelopmentOrder devOrder = developmentOrderRepository.findById(request.developmentOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "현상 주문을 찾을 수 없습니다."));

        if (!devOrder.getUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "주문 권한이 없습니다.");
        }

        // 2) 선택 사진 검증 (중복 방어 + 접근 가능 count)
        if (request.photos() == null || request.photos().isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "인화할 사진은 최소 1장 이상 선택해야 합니다.");
        }

        List<Long> photoIds = request.photos().stream()
                .map(PhotoRequest.SelectedPhoto::scannedPhotoId)
                .distinct()
                .toList();

        long validCount = scannedPhotoRepository.countAccessiblePhotos(
                memberId,
                devOrder.getId(),
                photoIds
        );

        if (validCount != photoIds.size()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 사진 중 유효하지 않은 항목이 있습니다.");
        }

        // 3) 가격 계산 (견적 로직과 동일)
        PrintPricePolicy.PriceResult price;
        try {
            price = printPricePolicy.calculate(request);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST, e.getMessage());
        }

        int unitPrice = price.unitPrice();
        int printTotalPrice = price.printTotalPrice();
        int deliveryFee = price.deliveryFee();
        int orderTotalPrice = price.orderTotalPrice();


        // 4) PrintOrder 생성/저장
        PrintOrder order = PrintOrder.create(
                devOrder,
                devOrder.getPhotoLab(),
                devOrder.getUser(),
                request.receiptMethod(),
                orderTotalPrice
        );
        printOrderRepository.save(order);

        // 5) PrintOrderItem 저장 (배송비 제외한 "인화 금액" 기준으로 저장)
        PrintOrderItem item = PrintOrderItem.create(
                order,
                request.filmType(),
                request.paperType(),
                request.printMethod(),
                request.size(),
                request.frameType(),
                unitPrice,
                printTotalPrice
        );
        printOrderItemRepository.save(item);

        // 6) PrintOrderPhoto 저장 (사진별 수량)
        for (PhotoRequest.SelectedPhoto p : request.photos()) {

            ScannedPhoto photo = scannedPhotoRepository.getReferenceById(p.scannedPhotoId());
            PrintOrderPhoto mapping = PrintOrderPhoto.create(order, photo, p.quantity());
            printOrderPhotoRepository.save(mapping);
        }

        if (request.receiptMethod() == ReceiptMethod.DELIVERY) {
            if (request.deliveryAddress() == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "배송 주문은 배송지 정보가 필수입니다.");
            }

            var a = request.deliveryAddress();

            Delivery delivery = Delivery.toEntity(
                    order,
                    a.recipientName(),
                    a.phone(),
                    a.zipcode(),
                    a.address(),
                    a.addressDetail(),
                    deliveryFee
            );
            deliveryRepository.save(delivery);
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


