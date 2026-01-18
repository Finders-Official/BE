package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.RestorationRequest;
import com.finders.api.domain.photo.dto.RestorationResponse;
import com.finders.api.domain.photo.dto.ShareResponse;
import com.finders.api.domain.photo.service.PhotoRestorationService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(
            summary = "사진 복원 요청",
            description = """
                AI 사진 복원을 요청합니다.

                ### 사전 작업 (프론트엔드)
                1. `POST /api/files/presigned-url` 로 원본/마스크 이미지 업로드 URL 발급
                2. 발급받은 URL로 GCS에 직접 PUT 업로드
                3. 업로드 완료 후 objectPath를 이 API로 전달

                ### 요청 예시
                ```json
                {
                  "originalPath": "restorations/123/original/uuid.png",
                  "maskPath": "restorations/123/mask/uuid.png"
                }
                ```
                """
    )
    @PostMapping
    public ApiResponse<RestorationResponse.Created> createRestoration(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid RestorationRequest.Create request
    ) {
        RestorationResponse.Created response = restorationService.createRestoration(user.memberId(), request);
        return ApiResponse.success(SuccessCode.RESTORATION_CREATED, response);
    }

    @Operation(summary = "복원 결과 조회", description = "특정 복원 요청의 상세 결과를 조회합니다.")
    @GetMapping("/{restorationId}")
    public ApiResponse<RestorationResponse.Detail> getRestoration(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long restorationId
    ) {
        RestorationResponse.Detail response = restorationService.getRestoration(user.memberId(), restorationId);
        return ApiResponse.success(SuccessCode.RESTORATION_FOUND, response);
    }

    @Operation(summary = "복원 이력 조회", description = "사용자의 모든 복원 이력을 조회합니다.")
    @GetMapping
    public ApiResponse<List<RestorationResponse.Summary>> getRestorationHistory(
            @AuthenticationPrincipal AuthUser user
    ) {
        List<RestorationResponse.Summary> response = restorationService.getRestorationHistory(user.memberId());
        return ApiResponse.ok(response);
    }

    @Operation(summary = "복원 결과 피드백", description = "복원 결과에 대한 피드백(좋음/나쁨)을 남깁니다.")
    @PostMapping("/{restorationId}/feedback")
    public ApiResponse<Void> addFeedback(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long restorationId,
            @RequestBody @Valid RestorationRequest.Feedback request
    ) {
        restorationService.addFeedback(user.memberId(), restorationId, request);
        return ApiResponse.success(SuccessCode.OK);
    }

    @Operation(
            summary = "복원 이미지 공유",
            description = """
                AI 복원 완료 이미지를 커뮤니티 공유용으로 Public 버킷에 복사합니다.

                ### 사용 시나리오
                1. "사진수다에 자랑하기" 버튼 클릭
                2. 이 API 호출 → objectPath, width, height 반환
                3. 게시글 작성 화면으로 이동 (이미지 정보 미리 채워짐)
                4. POST /posts API로 게시글 작성

                ### 응답 예시
                ```json
                {
                  "objectPath": "temp/123/abc.png",
                  "width": 1920,
                  "height": 1080
                }
                ```

                ### 참고
                - Private 버킷에서 Public 버킷으로 GCS 내부 복사 (빠르고 비용 없음)
                - 반환된 objectPath를 게시글 작성 API에 그대로 전달
                - temp/ 경로로 복사되며 30일 후 자동 삭제 (게시글 작성 시 영구 경로로 이동)
                """
    )
    @PostMapping("/{restorationId}/share")
    public ApiResponse<ShareResponse> shareToPublic(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long restorationId
    ) {
        ShareResponse response = restorationService.shareToPublic(user.memberId(), restorationId);
        return ApiResponse.success(SuccessCode.OK, response);
    }
}
