package com.finders.api.domain.file.dto;

import com.finders.api.infra.storage.StoragePath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FileRequest {
    // 단건 업로드
    public record GetPresignedUrl(
            @Schema(description = "업로드 카테고리 (PROFILE, POST_IMAGE 등)")
            @NotNull(message = "카테고리는 필수입니다.")
            StoragePath category,

            @Schema(description = "파일명 (확장자 포함)")
            @NotBlank(message = "파일명은 필수입니다.")
            String fileName,

            @Schema(description = "파일을 소유할 회원의 ID (현재 로그인한 유저 본인의 ID)")
            @NotNull(message = "회원 ID는 필수입니다.")
            Long memberId
    ) {}

    // 벌크 업로드
    public record GetBulkPresignedUrls(
            @NotNull StoragePath category,
            @NotNull Long domainId,
            @Min(1) int count   // 발급받을 URL 개수
    ) {}
}
