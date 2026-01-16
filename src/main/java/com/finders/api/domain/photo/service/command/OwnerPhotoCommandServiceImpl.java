package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.photo.dto.OwnerPhotoRequest;
import com.finders.api.domain.photo.dto.OwnerPhotoResponse;
import com.finders.api.domain.photo.entity.DevelopmentOrder;
import com.finders.api.domain.photo.entity.ScannedPhoto;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.repository.DevelopmentOrderRepository;
import com.finders.api.domain.photo.repository.ScannedPhotoRepository;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.repository.ReservationRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnerPhotoCommandServiceImpl implements OwnerPhotoCommandService {

    private final PhotoLabRepository photoLabRepository;
    private final ReservationRepository reservationRepository;
    private final MemberUserRepository memberUserRepository;
    private final DevelopmentOrderRepository developmentOrderRepository;
    private final ScannedPhotoRepository scannedPhotoRepository;
    private final StorageService storageService;

    private static final DateTimeFormatter ORDER_DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    @Override
    public OwnerPhotoResponse.PresignedUrls createScanUploadPresignedUrls(
            Long photoLabId,
            Long ownerId,
            OwnerPhotoRequest.CreateScanUploadPresignedUrls request
    ) {
        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLab.getOwner() == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.PHOTO_OWNER_MISMATCH);
        }

        DevelopmentOrder order = developmentOrderRepository.findById(request.developmentOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_ORDER_NOT_FOUND));

        if (!order.getPhotoLab().getId().equals(photoLabId)) {
            throw new CustomException(ErrorCode.PHOTO_ORDER_PHOTOLAB_MISMATCH);
        }

        int count = request.count();
        String orderCode = order.getOrderCode();

        List<OwnerPhotoResponse.Item> items = new ArrayList<>(count);
        Long expiresAt = null;

        for (int i = 1; i <= count; i++) {
            String filename = UUID.randomUUID().toString().replace("-", "");

            String objectPath = String.format("scanned/%d/orders/%s/%s", photoLabId, orderCode, filename);

            StorageResponse.SignedUrl signed = storageService.getSignedUrl(objectPath, null);
            if (expiresAt == null) expiresAt = signed.expiresAtEpochSecond();

            items.add(OwnerPhotoResponse.Item.of(i, objectPath, signed.url()));
        }

        return OwnerPhotoResponse.PresignedUrls.of(
                photoLabId,
                order.getId(),
                orderCode,
                expiresAt,
                items
        );
    }

    @Override
    public Long createDevelopmentOrder(
            Long photoLabId,
            Long ownerId,
            OwnerPhotoRequest.CreateDevelopmentOrder request
    ) {
        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLab.getOwner() == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.PHOTO_OWNER_MISMATCH);
        }

        Reservation reservation = null;
        MemberUser memberUser;

        if (request.reservationId() != null) {
            reservation = reservationRepository.findById(request.reservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

            if (!reservation.getPhotoLab().getId().equals(photoLabId)) {
                throw new CustomException(ErrorCode.PHOTO_RESERVATION_MISMATCH);
            }

            memberUser = reservation.getUser();
        } else {
            if (request.memberId() == null) {
                throw new CustomException(ErrorCode.PHOTO_MEMBER_REQUIRED);
            }

            memberUser = memberUserRepository.findById(request.memberId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        }

        String orderCode = generateUniqueOrderCode(photoLabId);

        DevelopmentOrder order = DevelopmentOrder.create(
                photoLab,
                memberUser,
                reservation,
                orderCode,
                request.totalPhotos(),
                request.totalPrice(),
                request.taskTypes(),
                request.rollCount() == null ? 0 : request.rollCount()
        );

        return developmentOrderRepository.save(order).getId();
    }


    @Override
    public OwnerPhotoResponse.ScannedPhotosRegistered registerScannedPhotos(
            Long photoLabId,
            Long ownerId,
            Long developmentOrderId,
            OwnerPhotoRequest.RegisterScannedPhotos request
    ) {
        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLab.getOwner() == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.PHOTO_OWNER_MISMATCH);
        }

        DevelopmentOrder order = developmentOrderRepository.findById(developmentOrderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));

        if (!order.getPhotoLab().getId().equals(photoLabId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<ScannedPhoto> photos = request.scannedPhotos().stream()
                .map(item -> ScannedPhoto.create(
                        order,
                        item.imageKey(),
                        item.fileName(),
                        item.displayOrder()
                ))
                .toList();

        scannedPhotoRepository.saveAll(photos);

        return OwnerPhotoResponse.ScannedPhotosRegistered.of(developmentOrderId, photos.size());
    }

    @Override
    public OwnerPhotoResponse.DevelopmentOrderStatusUpdated updateDevelopmentOrderStatus(
            Long photoLabId,
            Long ownerId,
            Long developmentOrderId,
            OwnerPhotoRequest.UpdateDevelopmentOrderStatus request
    ) {
        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLab.getOwner() == null || !photoLab.getOwner().getId().equals(ownerId)) {
            throw new CustomException(ErrorCode.PHOTO_OWNER_MISMATCH);
        }

        DevelopmentOrder order = developmentOrderRepository.findById(developmentOrderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));
        // 원하면 PHOTO_NOT_FOUND 대신 DEVELOPMENT_ORDER_NOT_FOUND 같은 코드 새로 파도 됨

        if (!order.getPhotoLab().getId().equals(photoLabId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        DevelopmentOrderStatus target = request.status();
        order.updateStatus(target);

        return OwnerPhotoResponse.DevelopmentOrderStatusUpdated.of(order.getId(), order.getStatus());
    }

    private String generateOrderCode(Long photoLabId) {
        String date = LocalDate.now().format(ORDER_DATE_FMT);
        int rand = (int) (Math.random() * 9000) + 1000;
        return "D" + photoLabId + date + rand;
    }

    private String generateUniqueOrderCode(Long photoLabId) {
        for (int i = 0; i < 3; i++) {
            String code = generateOrderCode(photoLabId);
            if (!developmentOrderRepository.existsByOrderCode(code)) {
                return code;
            }
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "주문 코드 생성에 실패했습니다.");
    }
}
