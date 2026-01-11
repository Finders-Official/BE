package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.response.PhotoLabPopularResponse;
import com.finders.api.domain.store.service.query.PhotoLabPopularQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "PhotoLab_USER", description = "현상소 API")
@RestController
@RequestMapping("/photo-labs")
@RequiredArgsConstructor
public class UserPhotoLabController {
    private final PhotoLabPopularQueryService photoLabPopularQueryService;

    @Operation(summary = "인기 현상소 조회 API")
    @GetMapping("/popular")
    public ApiResponse<List<PhotoLabPopularResponse.Card>> getPopularPhotoLabs() {
        return ApiResponse.success(
                SuccessCode.OK,
                photoLabPopularQueryService.getPopularPhotoLabs()
        );
    }
}
