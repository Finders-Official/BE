package com.finders.api.domain.photo.controller;

import com.finders.api.domain.photo.dto.request.RestorationRequest;
import com.finders.api.domain.photo.dto.response.RestorationResponse;
import com.finders.api.domain.photo.dto.response.ShareResponse;
import com.finders.api.domain.photo.service.command.PhotoRestorationCommandService;
import com.finders.api.domain.photo.service.command.PhotoRestorationShareService;
import com.finders.api.domain.photo.service.query.PhotoRestorationQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * 사진 복원 API 컨트롤러
 */
@Tag(name = "Photo Restoration", description = "AI 사진 복원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/restorations")
public class PhotoRestorationController {

    private final PhotoRestorationCommandService commandService;
    private final PhotoRestorationQueryService queryService;
    private final PhotoRestorationShareService shareService;

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

                ### 응답
                - HTTP 201 Created
                - Location 헤더: 생성된 복원 리소스 URL
                """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RestorationResponse.Created>> createRestoration(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid RestorationRequest.Create request
    ) {
        RestorationResponse.Created response = commandService.createRestoration(user.memberId(), request);

        // Location 헤더 생성: /restorations/{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponse.success(SuccessCode.RESTORATION_CREATED, response));
    }

    @Operation(summary = "복원 결과 조회", description = "특정 복원 요청의 상세 결과를 조회합니다.")
    @GetMapping("/{restorationId}")
    public ApiResponse<RestorationResponse.Detail> getRestoration(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long restorationId
    ) {
        RestorationResponse.Detail response = queryService.getRestoration(user.memberId(), restorationId);
        return ApiResponse.success(SuccessCode.RESTORATION_FOUND, response);
    }

    @Operation(
            summary = "복원 이력 조회",
            description = """
                사용자의 복원 이력을 페이지네이션으로 조회합니다.

                ### 파라미터
                - page: 페이지 번호 (0부터 시작, 기본값: 0)
                - size: 페이지 크기 (기본값: 10)
                - 정렬: 최신순 고정 (createdAt DESC)

                ### 응답
                - content: 복원 이력 목록
                - totalElements: 전체 복원 건수
                - totalPages: 전체 페이지 수
                - size: 페이지 크기
                - number: 현재 페이지 번호
                """
    )
    @GetMapping
    public PagedResponse<RestorationResponse.Summary> getRestorationHistory(
            @AuthenticationPrincipal AuthUser user,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RestorationResponse.Summary> response = queryService.getRestorationHistory(user.memberId(), pageable);
        return PagedResponse.of(SuccessCode.RESTORATION_HISTORY_FOUND, response);
    }

    @Operation(summary = "복원 결과 피드백", description = "복원 결과에 대한 피드백(좋음/나쁨)을 남깁니다.")
    @PostMapping("/{restorationId}/feedback")
    public ApiResponse<Void> addFeedback(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long restorationId,
            @RequestBody @Valid RestorationRequest.Feedback request
    ) {
        commandService.addFeedback(user.memberId(), restorationId, request);
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

                ### 응답
                - HTTP 200 OK
                - X-Public-Image-URL 헤더: 공유된 이미지의 Public URL

                ### 참고
                - Private 버킷에서 Public 버킷으로 GCS 내부 복사 (빠르고 비용 없음)
                - 반환된 objectPath를 게시글 작성 API에 그대로 전달
                - temp/ 경로로 복사되며 30일 후 자동 삭제 (게시글 작성 시 영구 경로로 이동)
                """
    )
    @PostMapping("/{restorationId}/share")
    public ResponseEntity<ApiResponse<ShareResponse>> shareToPublic(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long restorationId
    ) {
        ShareResponse response = shareService.shareToPublic(user.memberId(), restorationId);

        // X-Public-Image-URL 헤더: 공유된 이미지의 Public URL
        String publicImageUrl = String.format("https://storage.googleapis.com/%s/%s",
                "finders-public-bucket", response.objectPath());

        return ResponseEntity.ok()
                .header("X-Public-Image-URL", publicImageUrl)
                .body(ApiResponse.success(SuccessCode.OK, response));
    }
}
