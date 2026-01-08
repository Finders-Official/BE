package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.PhotoLabRequest;
import com.finders.api.domain.store.dto.PhotoLabResponse;
import com.finders.api.domain.store.service.PhotoLabService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PhotoLab", description = "현상소 등록/관리 API")
@RestController
@RequestMapping("owner/photo-labs")
@RequiredArgsConstructor
public class PhotoLabController {

    private final PhotoLabService photoLabService;

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
}
