package com.finders.api.infra.storage;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage 테스트용 Controller (local 프로필 전용)
 * <p>
 * 프로덕션에서는 비활성화됩니다.
 * 실제 사용 시에는 각 도메인 Controller에서 StorageService를 주입받아 사용하세요.
 */
@Tag(name = "[TEST] Storage", description = "스토리지 테스트 API (local 전용)")
@Profile("local")
@RestController
@RequestMapping("/storage/test")
@RequiredArgsConstructor
public class StorageTestController {

    private final StorageService storageService;

    @Operation(summary = "Public 업로드 테스트", description = "Public 버킷에 파일 업로드 (프로필 경로)")
    @PostMapping(value = "/upload/public", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StorageResponse.Upload>> uploadPublic(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "1") Long testId
    ) {
        StorageResponse.Upload result = storageService.uploadPublic(
                file,
                StoragePath.PROFILE,
                testId
        );
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORAGE_UPLOADED, result));
    }

    @Operation(summary = "Private 업로드 테스트", description = "Private 버킷에 파일 업로드 (스캔 경로)")
    @PostMapping(value = "/upload/private", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StorageResponse.Upload>> uploadPrivate(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "1") Long testId
    ) {
        StorageResponse.Upload result = storageService.uploadPrivate(
                file,
                StoragePath.SCANNED_PHOTO,
                testId
        );
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORAGE_UPLOADED, result));
    }

    @Operation(summary = "Signed URL 생성 테스트", description = "Private 파일의 Signed URL 생성")
    @PostMapping("/signed-url")
    public ResponseEntity<ApiResponse<StorageResponse.SignedUrl>> getSignedUrl(
            @RequestParam String objectPath,
            @RequestParam(required = false) Integer expiryMinutes
    ) {
        StorageResponse.SignedUrl result = storageService.getSignedUrl(objectPath, expiryMinutes);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
    }

    @Operation(summary = "파일 삭제 테스트", description = "버킷에서 파일 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<StorageResponse.Delete>> delete(
            @RequestParam String objectPath,
            @RequestParam(defaultValue = "true") boolean isPublic
    ) {
        StorageResponse.Delete result = storageService.delete(objectPath, isPublic);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORAGE_DELETED, result));
    }
}
