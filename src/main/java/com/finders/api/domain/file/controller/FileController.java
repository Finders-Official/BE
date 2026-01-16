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
            description = "GCS에 직접 파일을 업로드하기 위한 임시 URL을 발급합니다."
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
            description = "본인 소유의 Private 파일에 접근하기 위한 임시 URL 발급"
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
