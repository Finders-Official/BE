package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.response.PhotoLabDocumentResponse;
import com.finders.api.domain.store.dto.response.PhotoLabImageResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.service.PhotoLabDocumentService;
import com.finders.api.domain.store.service.PhotoLabImageService;
import com.finders.api.domain.store.service.PhotoLabService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import com.finders.api.infra.storage.StorageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PhotoLab_OWNER", description = "owner용 현상소 API")
@RestController
@RequestMapping("/owner/photo-labs")
@RequiredArgsConstructor
public class OwnerPhotoLabController {

    private final PhotoLabService photoLabService;
    private final PhotoLabImageService photoLabImageService;
    private final PhotoLabDocumentService photoLabDocumentService;

    @Operation(
            summary = "현상소 기본사항 등록 API",
            description = "현상소의 기본사항을 등록합니다.")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ApiResponse<PhotoLabResponse.Create> createPhotoLab(
            @AuthenticationPrincipal AuthUser owner,
            @Valid @RequestBody PhotoLabRequest.Create request
    ) {
        return ApiResponse.success(
                SuccessCode.CREATED,
                photoLabService.createPhotoLab(owner.memberId(), request)
        );
    }
    @Operation(summary = "현상소 이미지 업로드 presigned url 발급")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/images/presigned-url")
    public ApiResponse<StorageResponse.PresignedUrl> createPhotoLabImagePresignedUrl(
            @AuthenticationPrincipal AuthUser owner,
            @RequestParam Long photoLabId,
            @RequestBody @Valid PhotoLabRequest.CreateImagePresignedUrl request
    ) {
        return ApiResponse.success(
                SuccessCode.STORAGE_UPLOAD_URL_ISSUED,
                photoLabImageService.createImagePresignedUrl(owner.memberId(), photoLabId, request.fileName())
        );
    }

    @Operation(
            summary = "현상소 이미지 등록",
            description = "현상소 이미지를 등록합니다.")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/images")
    public ApiResponse<PhotoLabImageResponse.Create> registerPhotoLabImage(
            @AuthenticationPrincipal AuthUser owner,
            @RequestParam Long photoLabId,
            @RequestBody @Valid PhotoLabRequest.RegisterImage request
    ) {
        return ApiResponse.success(
                SuccessCode.STORAGE_UPLOADED,
                photoLabImageService.registerImage(
                        owner.memberId(),
                        photoLabId,
                        request.objectPath(),
                        request.displayOrder(),
                        request.isMain()
                )
        );
    }

    @Operation(summary = "현상소 사업자 등록 서류 업로드 presigned url 발급 API")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/documents/presigned-url")
    public ApiResponse<StorageResponse.PresignedUrl> createPhotoLabDocumentPresignedUrl(
            @AuthenticationPrincipal AuthUser owner,
            @RequestParam Long photoLabId,
            @RequestBody @Valid PhotoLabRequest.CreateDocumentPresignedUrl request
    ) {
        return ApiResponse.success(
                SuccessCode.STORAGE_UPLOAD_URL_ISSUED,
                photoLabDocumentService.createDocumentPresignedUrl(
                        owner.memberId(),
                        photoLabId,
                        request.documentType(),
                        request.fileName()
                )
        );
    }

    @Operation(
            summary = "현상소 사업자 서류 등록",
            description = "현상소 사업자 등록 서류를 등록합니다.")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/documents")
    public ApiResponse<PhotoLabDocumentResponse.Create> registerPhotoLabDocument(
            @AuthenticationPrincipal AuthUser owner,
            @RequestParam Long photoLabId,
            @RequestBody @Valid PhotoLabRequest.RegisterDocument request
    ) {
        return ApiResponse.success(
                SuccessCode.STORAGE_UPLOADED,
                photoLabDocumentService.registerDocument(
                        owner.memberId(),
                        photoLabId,
                        request.documentType(),
                        request.objectPath(),
                        request.fileName()
                )
        );
    }
}