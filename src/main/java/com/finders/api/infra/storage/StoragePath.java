package com.finders.api.infra.storage;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * GCS 저장 경로 패턴
 * <p>
 * ERD.md의 GCS 규칙에 따른 경로 정의.
 * isPublic=true: finders-public 버킷 (직접 URL 접근)
 * isPublic=false: finders-private 버킷 (Signed URL 필요)
 */
@Getter
@RequiredArgsConstructor
public enum StoragePath {

    // ========================================
    // Public Bucket Paths (직접 URL 접근)
    // ========================================

    /** 프로필 이미지: profiles/{memberId}/{uuid}.{ext} */
    PROFILE("profiles/%d/%s", true),

    /** 현상소 이미지: photo-labs/{photoLabId}/images/{uuid}.{ext} */
    LAB_IMAGE("photo-labs/%d/images/%s", true),

    /** 현상소 QR 코드: photo-labs/{photoLabId}/qr.png */
    LAB_QR("photo-labs/%d/qr.png", true),

    /** 게시글 이미지: posts/{memberId}/{uuid}.{ext} */
    POST_IMAGE("posts/%d/%s", true),

    /** 프로모션 이미지: promotions/{promotionId}/{uuid}.{ext} */
    PROMOTION("promotions/%d/%s", true),

    /** 임시 업로드 (public): temp/{memberId}/{uuid}.{ext} - 30일 후 자동 삭제 */
    TEMP_PUBLIC("temp/%d/%s", true),

    // ========================================
    // Private Bucket Paths (Signed URL 필요)
    // ========================================

    /** 현상소 증빙서류: photo-labs/{photoLabId}/documents/{documentType}/{uuid}.{ext} */
    LAB_DOCUMENT("photo-labs/%d/documents/%s/%s", false),

    /** 스캔 사진: temp/orders/{developmentOrderId}/scans/{uuid}.{ext} - 30일 후 자동 삭제 */
    SCANNED_PHOTO("temp/orders/%d/scans/%s", false),

    /** AI 복원 원본: restorations/{memberId}/original/{uuid}.{ext} */
    RESTORATION_ORIGINAL("restorations/%d/original/%s", false),

    /** AI 복원 마스크: restorations/{memberId}/mask/{uuid}.{ext} */
    RESTORATION_MASK("restorations/%d/mask/%s", false),

    /** AI 복원 결과: restorations/{memberId}/restored/{uuid}.{ext} */
    RESTORATION_RESTORED("restorations/%d/restored/%s", false);

    private final String pattern;
    private final boolean isPublic;

    /**
     * 경로 생성
     *
     * @param args pattern에 대입할 인자들 (마지막 인자는 보통 파일명)
     * @return 포맷된 경로
     */
    public String format(Object... args) {
        return String.format(pattern, args);
    }

    // 경로 문자열 분석 -> 해당하는 StoragePath 상수 반환
    public static StoragePath fromObjectPath(String objectPath) {
        return Arrays.stream(values())
                .filter(path -> {
                    String root = path.getPattern().split("/")[0];
                    if (root.equals("temp") && objectPath.startsWith("temp/orders")) {
                        return path == SCANNED_PHOTO;
                    }
                    return objectPath.startsWith(root);
                })
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORAGE_INVALID_PATH));
    }

    public Long extractId(String objectPath) {
        try {
            String[] parts = objectPath.split("/");
            if (this == SCANNED_PHOTO) return Long.parseLong(parts[2]); // temp/orders/{id}
            return Long.parseLong(parts[1]); // 그 외 보통 {category}/{id}
        } catch (Exception e) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_PATH);
        }
    }

    // 공통 API 사용 가능 여부 확인
    public boolean isCommon() {
        return this == PROFILE || this == POST_IMAGE || this == TEMP_PUBLIC ||
                this == RESTORATION_ORIGINAL || this == RESTORATION_MASK || this == RESTORATION_RESTORED;
    }
}
