package com.finders.api.domain.file.controller;

import com.finders.api.domain.file.dto.FileRequest;
import com.finders.api.domain.file.service.command.FileCommandService;
import com.finders.api.domain.file.service.query.FileQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import com.finders.api.infra.storage.StorageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "File", description = "파일 업로드 및 관리 API")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileCommandService fileCommandService;
    private final FileQueryService fileQueryService;

    /**
     * 공통 업로드용 Presigned URL 발급 API
     * 프로필, 커뮤니티 게시글, 문의사항 등 일반적인 용도로 사용
     */
    @Operation(
            summary = "업로드용 Presigned URL 발급",
            description = """
                GCS에 직접 파일을 업로드하기 위한 임시 URL을 발급합니다.
                
                ### [허용되는 카테고리 (domain)]
                - **PROFILE**: 유저 프로필 이미지
                - **POST_IMAGE**: 커뮤니티 게시글 이미지
                - **TEMP_PUBLIC**: 30일 후 자동 삭제되는 공통 임시 파일
                - **RESTORATION_ORIGINAL**: AI 복원 요청 원본 사진
                - **RESTORATION_MASK**: AI 복원용 마스크 데이터
                - **RESTORATION_RESTORED**: AI 복원 완료된 결과물
                
                ⚠️ **주의**: 그 외 카테고리(SCANNED_PHOTO, LAB_DOCUMENT 등)는 도메인 전용 API를 사용해야 합니다.
                """
    )
    @PostMapping("/presigned-url")
    public ApiResponse<StorageResponse.PresignedUrl> getPresignedUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid FileRequest.GetPresignedUrl request
    ) {
        StorageResponse.PresignedUrl response = fileCommandService.getPresignedUrl(authUser.memberId(), request);
        return ApiResponse.success(SuccessCode.STORAGE_UPLOAD_URL_ISSUED, response);
    }

    @Operation(
            summary = "Private 파일 조회 URL 발급",
            description = """
                본인 소유의 비공개 버킷 파일에 접근하기 위한 임시 Signed URL을 발급합니다.
                발급된 URL은 **15분간 유효**합니다.
                
                ### [조회 가능한 주요 파일]
                - **RESTORATION_ORIGINAL**: AI 복원 요청 원본 (Private)
                - **RESTORATION_MASK**: AI 복원 마스크 (Private)
                - **RESTORATION_RESTORED**: AI 복원 결과물 (Private)
                
                ### [사용 방법]
                1. DB에 저장된 `objectPath`(예: `restorations/1/original/uuid.jpg`)를 파라미터로 전달
                2. 응답으로 받은 `signedUrl`을 이미지 태그의 `src` 등으로 사용
                
                ⚠️ **주의**: `isCommon`이 `false`인 도메인 전용 파일(현상소 증빙 서류 등)은 본 API로 조회가 불가능합니다.
                """
    )
    @GetMapping("/signed-url")
    public ApiResponse<StorageResponse.SignedUrl> getSignedUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String objectPath
    ) {
        return ApiResponse.success(SuccessCode.OK,
                fileQueryService.getSignedUrl(authUser.memberId(), objectPath));
    }
}
