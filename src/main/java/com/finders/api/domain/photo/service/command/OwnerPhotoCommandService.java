package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.OwnerPhotoRequest;
import com.finders.api.domain.photo.dto.OwnerPhotoResponse;

public interface OwnerPhotoCommandService {

    OwnerPhotoResponse.PresignedUrls createScanUploadPresignedUrls(
            Long photoLabId,
            Long ownerId,
            OwnerPhotoRequest.CreateScanUploadPresignedUrls request
    );

    Long createDevelopmentOrder(
            Long photoLabId,
            Long ownerId,
            OwnerPhotoRequest.CreateDevelopmentOrder request
    );

    OwnerPhotoResponse.ScannedPhotosRegistered registerScannedPhotos(
            Long photoLabId,
            Long ownerId,
            Long developmentOrderId,
            OwnerPhotoRequest.RegisterScannedPhotos request
    );

    OwnerPhotoResponse.DevelopmentOrderStatusUpdated updateDevelopmentOrderStatus(
            Long photoLabId,
            Long ownerId,
            Long developmentOrderId,
            OwnerPhotoRequest.UpdateDevelopmentOrderStatus request
    );
}
