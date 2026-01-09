package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.response.PhotoLabDocumentResponse;
import com.finders.api.domain.store.dto.response.PhotoLabImageResponse;
import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.enums.DocumentType;
import com.finders.api.domain.store.service.PhotoLabDocumentService;
import com.finders.api.domain.store.service.PhotoLabImageService;
import com.finders.api.domain.store.service.PhotoLabService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "PhotoLab_OWNER", description = "현상소 등록/관리 API")
@RestController
@RequestMapping("/owner/photo-labs")
@RequiredArgsConstructor
public class PhotoLabController {

    private final PhotoLabService photoLabService;
    private final PhotoLabImageService photoLabImageService;
    private final PhotoLabDocumentService photoLabDocumentService;

    @Operation(summary = "현상소 기본사항 등록 API")
    @PostMapping
    public ApiResponse<PhotoLabResponse.Create> createPhotoLab(
            @Valid @RequestBody PhotoLabRequest.Create request
    ) {
        return ApiResponse.success(
                SuccessCode.CREATED,
                photoLabService.createPhotoLab(request)
        );
    }

    @Operation(summary = "현상소 이미지 등록 API")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PhotoLabImageResponse.Create> uploadPhotoLabImage(
            @RequestParam Long photoLabId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Integer displayOrder,
            @RequestParam(defaultValue = "false") boolean isMain
    ) {
        return ApiResponse.success(
                SuccessCode.STORAGE_UPLOADED,
                photoLabImageService.uploadImage(photoLabId, file, displayOrder, isMain)
        );
    }

    @Operation(summary = "현상소 사업자 등록 서류 업로드 API")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PhotoLabDocumentResponse.Create> uploadPhotoLabDocument(
            @RequestParam Long photoLabId,
            @RequestParam DocumentType documentType,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(
                SuccessCode.STORAGE_UPLOADED,
                photoLabDocumentService.uploadDocument(photoLabId, documentType, file)
        );
    }
}
