package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.photo.dto.PhotoRequest;
import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.entity.Delivery;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.PrintOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.photo.enums.print.FilmType;
import com.finders.api.domain.photo.enums.print.FrameType;
import com.finders.api.domain.photo.enums.print.PaperType;
import com.finders.api.domain.photo.enums.print.PrintMethod;
import com.finders.api.domain.photo.enums.print.PrintSize;
import com.finders.api.domain.photo.policy.PrintPricePolicy;
import com.finders.api.domain.photo.repository.DeliveryRepository;
import com.finders.api.domain.photo.repository.DevelopmentOrderRepository;
import com.finders.api.domain.photo.repository.PrintOrderRepository;
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
    private static final int DELIVERY_FEE = 3000;
    private final PrintPricePolicy printPricePolicy;

    private final DevelopmentOrderRepository developmentOrderRepository;
    private final ScannedPhotoRepository scannedPhotoRepository;
    private final PrintOrderRepository printOrderRepository;
    private final DeliveryRepository deliveryRepository;
    private final StorageService storageService;

    @Override
    public PhotoResponse.MyCurrentWork getMyCurrentWork(Long memberId) {

        // 1) 진행중 DevelopmentOrder 1건 조회 (없으면 null)
        DevelopmentOrder order = developmentOrderRepository
                .findByUserIdAndStatusNot(memberId, DevelopmentOrderStatus.COMPLETED)
                .orElse(null);

        if (order == null) {
            return null; // 프론트에서 진행중 화면 없으면 지난작업으로 보내는 구조면 null OK
        }

        // 2) 인화 주문 조회 (인화가 아닌 주문이면 조회 자체 스킵)
        PrintOrder printOrder = null;
        if (order.isPrint()) {
            printOrder = printOrderRepository.findByDevelopmentOrderId(order.getId())
                    .orElse(null);
        }

        PhotoResponse.PrintProgress printProgress = (printOrder != null)
                ? PhotoResponse.PrintProgress.from(printOrder)
                : null;

        // 3) 배송 조회 (인화 + 배송 수령 방식일 때만 조회)
        Delivery delivery = null;
        if (printOrder != null && printOrder.getReceiptMethod() == ReceiptMethod.DELIVERY) {
            delivery = deliveryRepository.findByPrintOrderId(printOrder.getId()).orElse(null);
        }

        PhotoResponse.DeliveryProgress deliveryProgress = (delivery != null)
                ? PhotoResponse.DeliveryProgress.from(delivery)
                : null;

        // 5) 응답 조합
        return PhotoResponse.MyCurrentWork.from(
                order,
                printProgress,
                deliveryProgress
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PhotoResponse.MyDevelopmentOrder> getMyDevelopmentOrders(Long memberId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<DevelopmentOrder> orderSlice =
                developmentOrderRepository.findMyOrdersWithPhotoLab(memberId, pageable);

        if (orderSlice.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        List<DevelopmentOrder> orders = orderSlice.getContent();
        List<Long> orderIds = orders.stream().map(DevelopmentOrder::getId).toList();

        // 1. 스캔 프리뷰
        List<ScanPreviewProjection> previews =
                scannedPhotoRepository.findPreviewByOrderIds(orderIds, PREVIEW_LIMIT);

        List<String> keys = previews.stream()
                .map(ScanPreviewProjection::getImageKey)
                .toList();

        Map<String, StorageResponse.SignedUrl> signedMap =
                storageService.getSignedUrls(keys, null);

        Map<Long, List<String>> previewUrlMap = new HashMap<>();
        for (ScanPreviewProjection p : previews) {
            StorageResponse.SignedUrl signed = signedMap.get(p.getImageKey());
            if (signed == null) continue;

            previewUrlMap.computeIfAbsent(p.getOrderId(), k -> new ArrayList<>())
                    .add(signed.url());
        }

        // 2) 인화(PRINT)인 주문만 PrintOrder + Delivery 조회

        List<Long> printTargetOrderIds = orders.stream()
                .filter(DevelopmentOrder::hasPrintTask)
                .map(DevelopmentOrder::getId)
                .toList();

        Map<Long, PrintOrder> printOrderByDevOrderId = new HashMap<>();
        Map<Long, Delivery> deliveryByPrintOrderId = new HashMap<>();

        if (!printTargetOrderIds.isEmpty()) {
            //  PrintOrder 배치 조회: dev_order_id IN (...)
            List<PrintOrder> printOrders = printOrderRepository.findByDevelopmentOrderIdIn(printTargetOrderIds);

            for (PrintOrder po : printOrders) {
                if (po.getDevelopmentOrder() == null) continue;
                printOrderByDevOrderId.put(po.getDevelopmentOrder().getId(), po);
            }

            // Delivery 배치 조회: print_order_id IN (...)
            List<Long> printOrderIds = printOrders.stream()
                    .map(PrintOrder::getId)
                    .toList();

            if (!printOrderIds.isEmpty()) {
                List<Delivery> deliveries = deliveryRepository.findByPrintOrderIdIn(printOrderIds);
                for (Delivery d : deliveries) {
                    // Delivery가 PrintOrder 연관을 갖고 있으니 여기서 id를 뽑아 map 구성
                    deliveryByPrintOrderId.put(d.getPrintOrder().getId(), d);
                }
            }
        }

        List<PhotoResponse.MyDevelopmentOrder> dtoList = orders.stream()
                .map(order -> {
                    List<String> previewUrls = previewUrlMap.getOrDefault(order.getId(), List.of());

                    PrintOrder po = printOrderByDevOrderId.get(order.getId()); // 인화 아니면 null
                    Delivery delivery = (po != null) ? deliveryByPrintOrderId.get(po.getId()) : null;

                    return PhotoResponse.MyDevelopmentOrder.from(order, previewUrls, delivery);
                })
                .toList();

        return new SliceImpl<>(dtoList, pageable, orderSlice.hasNext());
    }


    @Override
    public Slice<PhotoResponse.ScanResult> getMyScanResults(Long memberId, Long developmentOrderId, int page, int size) {

        boolean isMine = developmentOrderRepository.existsByIdAndUser_Id(developmentOrderId, memberId);
        if (!isMine) throw new CustomException(ErrorCode.FORBIDDEN, "해당 주문에 접근 권한이 없습니다.");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));

        Slice<ScannedPhoto> slice =
                scannedPhotoRepository.findByOrderIdOrderByDisplayOrderAsc(developmentOrderId, pageable);

        List<String> keys = slice.getContent().stream()
                .map(ScannedPhoto::getObjectPath)
                .toList();

        Map<String, StorageResponse.SignedUrl> signedMap =
                storageService.getSignedUrls(keys, null);

        List<PhotoResponse.ScanResult> content = slice.getContent().stream()
                .map(photo -> {
                    StorageResponse.SignedUrl signed = signedMap.get(photo.getObjectPath());
                    if (signed == null) {
                        // 사진이 없는 경우
                        return PhotoResponse.ScanResult.from(photo, null, null);
                    }
                    return PhotoResponse.ScanResult.from(photo, signed.url(), signed.expiresAtEpochSecond());
                })
                .toList();

        return new SliceImpl<>(content, pageable, slice.hasNext());
    }

    @Override
    public PhotoResponse.PrintOptions getPrintOptions() {
        // 지금은 고정 가격표. (나중에 photoLab별 정책으로 바꾸면 여기만 수정)
        return PhotoResponse.PrintOptions.builder()
                .deliveryFee(DELIVERY_FEE)
                .filmTypes(List.of(
                        // 필름: 비율 + 절사 정책
                        PhotoResponse.PrintOptionItem.flat(FilmType.SLIDE.name(), "슬라이드", 0),
                        PhotoResponse.PrintOptionItem.rate(FilmType.COLOR_NEG.name(), "컬러네가", 0.1, "FLOOR_100"),
                        PhotoResponse.PrintOptionItem.rate(FilmType.BLACK_WHITE.name(), "흑백", 0.1, "FLOOR_100")
                ))
                .printMethods(List.of(
                        PhotoResponse.PrintOptionItem.flat(PrintMethod.INKJET.name(), "잉크젯", PrintMethod.INKJET.extraPrice()),
                        PhotoResponse.PrintOptionItem.flat(PrintMethod.CPRINT.name(), "CPRINT", PrintMethod.CPRINT.extraPrice())
                ))
                .paperTypes(List.of(
                        PhotoResponse.PrintOptionItem.flat(PaperType.ECO_GLOSSY_260.name(), "에코 글로시 260", PaperType.ECO_GLOSSY_260.extraPrice()),
                        PhotoResponse.PrintOptionItem.flat(PaperType.ECO_LUSTER_255.name(), "에코 러스터 255", PaperType.ECO_LUSTER_255.extraPrice()),
                        PhotoResponse.PrintOptionItem.flat(PaperType.EPSON_SEMIGLOSSY_250.name(), "앱손 세미글로시 250", PaperType.EPSON_SEMIGLOSSY_250.extraPrice())
                ))
                .sizes(List.of(
                        PhotoResponse.PrintOptionItem.size(PrintSize.SIZE_5x7.name(), "5*7", PrintSize.SIZE_5x7.basePrice()),
                        PhotoResponse.PrintOptionItem.size(PrintSize.SIZE_6x8.name(), "6*8", PrintSize.SIZE_6x8.basePrice()),
                        PhotoResponse.PrintOptionItem.size(PrintSize.SIZE_8x10.name(), "8*10", PrintSize.SIZE_8x10.basePrice()),
                        PhotoResponse.PrintOptionItem.size(PrintSize.SIZE_8x12.name(), "8*12", PrintSize.SIZE_8x12.basePrice()),
                        PhotoResponse.PrintOptionItem.size(PrintSize.A4.name(), "A4", PrintSize.A4.basePrice()),
                        PhotoResponse.PrintOptionItem.size(PrintSize.SIZE_10x15.name(), "10*15", PrintSize.SIZE_10x15.basePrice()),
                        PhotoResponse.PrintOptionItem.size(PrintSize.SIZE_11x14.name(), "11*14", PrintSize.SIZE_11x14.basePrice())
                ))
                .frameTypes(List.of(
                        PhotoResponse.PrintOptionItem.flat(FrameType.WHITE_FRAME.name(), "흰색 프레임", FrameType.WHITE_FRAME.extraPrice()),
                        PhotoResponse.PrintOptionItem.flat(FrameType.NO_FRAME.name(), "프레임 없음", FrameType.NO_FRAME.extraPrice())
                ))
                .build();
    }

    @Override
    public PhotoResponse.PrintQuote quote(Long memberId, PhotoRequest.PrintQuote request) {

        List<Long> photoIds = request.photos().stream()
                .map(PhotoRequest.SelectedPhoto::scannedPhotoId)
                .toList();

        long validCount = scannedPhotoRepository.countAccessiblePhotos(
                memberId,
                request.developmentOrderId(),
                photoIds
        );

        if (validCount != photoIds.size()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 사진 목록에 유효하지 않은 항목이 포함되어 있습니다.");
        }

        PrintPricePolicy.PriceResult price = printPricePolicy.calculate(request);

        return PhotoResponse.PrintQuote.builder()
                .printAmount(price.printTotalPrice())
                .deliveryFee(price.deliveryFee())
                .totalAmount(price.orderTotalPrice())
                .build();
    }


    @Override
    public PhotoResponse.PhotoLabAccount getPhotoLabAccount(Long memberId, Long developmentOrderId) {

        DevelopmentOrder order = developmentOrderRepository
                .findByIdAndMemberIdFetchPhotoLabOwner(developmentOrderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_PHOTOLAB_ACCOUNT_ACCESS_DENIED));

        MemberOwner owner = order.getPhotoLab().getOwner();

         if (owner.getBankAccountNumber() == null) throw new CustomException(ErrorCode.PHOTO_PHOTOLAB_ACCOUNT_NOT_REGISTERED);

        return new PhotoResponse.PhotoLabAccount(
                owner.getBankName(),
                owner.getBankAccountNumber(),
                owner.getBankAccountHolder()
        );
    }
}