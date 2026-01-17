package com.finders.api.domain.photo.service.query;

import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.photo.dto.PhotoRequest;
import com.finders.api.domain.photo.dto.PhotoResponse;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.photo.enums.print.FilmType;
import com.finders.api.domain.photo.enums.print.FrameType;
import com.finders.api.domain.photo.enums.print.PaperType;
import com.finders.api.domain.photo.enums.print.PrintMethod;
import com.finders.api.domain.photo.enums.print.PrintSize;
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
    private static final int DELIVERY_FEE = 3000;
    private static final int DEFAULT_BASE_PRICE = 1400;

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

        // 1) 선택 사진 목록 추출
        List<Long> photoIds = request.photos().stream()
                .map(PhotoRequest.SelectedPhoto::scannedPhotoId)
                .toList();

        // 2) 사진 소속/권한 검증 (한방 count)
        long validCount = scannedPhotoRepository.countAccessiblePhotos(
                memberId,
                request.developmentOrderId(),
                photoIds
        );

        if (validCount != photoIds.size()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 사진 목록에 유효하지 않은 항목이 포함되어 있습니다.");
        }

        // 3) 총 장수
        int totalQty = request.photos().stream()
                .mapToInt(p -> {
                    if (p.quantity() < 1) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "사진 수량(quantity)은 1 이상이어야 합니다.");
                    }
                    return p.quantity();
                })
                .sum();

        // 4) 단가 계산 (null-safe)
        int base = (request.size() != null)
                ? request.size().basePrice()
                : DEFAULT_BASE_PRICE;

        int filmExtra = (request.filmType() != null)
                ? request.filmType().extra(base)
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

        int unitPrice = base + filmExtra + methodExtra + paperExtra + frameExtra;

        int printTotal = unitPrice * totalQty;
        int deliveryFee = (request.receiptMethod() == ReceiptMethod.DELIVERY) ? DELIVERY_FEE : 0;
        int grandTotal = printTotal + deliveryFee;

        return PhotoResponse.PrintQuote.builder()
                .printAmount(printTotal)
                .deliveryFee(deliveryFee)
                .totalAmount(grandTotal)
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