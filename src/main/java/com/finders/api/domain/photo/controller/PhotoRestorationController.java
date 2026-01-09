package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.service.PhotoRestorationService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사진 복원 API 컨트롤러
 */
@Tag(name = "Photo Restoration", description = "AI 사진 복원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/restorations")
public class PhotoRestorationController {

    private final PhotoRestorationService restorationService;

    // TODO: JWT 인증 구현 시 @AuthenticationPrincipal로 교체
    private static final Long TEMP_MEMBER_ID = 1L;

    @Operation(summary = "사진 복원 요청", description = "손상된 사진을 업로드하여 AI 복원을 요청합니다. 과노출 영역이 자동으로 감지됩니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RestorationResponse.Created> createRestoration(
            @ModelAttribute @Valid RestorationRequest.Create request
    ) {
        RestorationResponse.Created response = restorationService.createRestoration(TEMP_MEMBER_ID, request);
        return ApiResponse.success(SuccessCode.RESTORATION_CREATED, response);
    }

    @Operation(summary = "복원 결과 조회", description = "특정 복원 요청의 상세 결과를 조회합니다.")
    @GetMapping("/{restorationId}")
    public ApiResponse<RestorationResponse.Detail> getRestoration(
            @PathVariable Long restorationId
    ) {
        RestorationResponse.Detail response = restorationService.getRestoration(TEMP_MEMBER_ID, restorationId);
        return ApiResponse.success(SuccessCode.RESTORATION_FOUND, response);
    }

    @Operation(summary = "복원 이력 조회", description = "사용자의 모든 복원 이력을 조회합니다.")
    @GetMapping
    public ApiResponse<List<RestorationResponse.Summary>> getRestorationHistory() {
        List<RestorationResponse.Summary> response = restorationService.getRestorationHistory(TEMP_MEMBER_ID);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "복원 결과 피드백", description = "복원 결과에 대한 피드백(좋음/나쁨)을 남깁니다.")
    @PostMapping("/{restorationId}/feedback")
    public ApiResponse<Void> addFeedback(
            @PathVariable Long restorationId,
            @RequestBody @Valid RestorationRequest.Feedback request
    ) {
        restorationService.addFeedback(TEMP_MEMBER_ID, restorationId, request);
        return ApiResponse.success(SuccessCode.OK);
    }
}
