package com.finders.api.infra.storage;

import lombok.Builder;

/**
 * Storage 응답 DTO
 */
public class StorageResponse {

    /**
     * 업로드 결과
     */
    @Builder
    public record Upload(
            String bucket,
            String objectPath,
            String url,           // public: 직접 URL, private: null
            String contentType,
            long size
    ) {
        public static Upload from(String bucket, String objectPath, String url,
                                  String contentType, long size) {
            return Upload.builder()
                    .bucket(bucket)
                    .objectPath(objectPath)
                    .url(url)
                    .contentType(contentType)
                    .size(size)
                    .build();
        }
    }

    /**
     * Signed URL 결과
     */
    public record SignedUrl(
            String url,
            long expiresAtEpochSecond
    ) {
    }

    @Builder
    public record PresignedUrl(
            String url,
            String objectPath,
            long expiresAtEpochSecond
    ) {
        public static PresignedUrl of(String url, String objectPath, long expiresAtEpochSecond) {
            return PresignedUrl.builder()
                    .url(url)
                    .objectPath(objectPath)
                    .expiresAtEpochSecond(expiresAtEpochSecond)
                    .build();
        }
    }

    /**
     * 삭제 결과
     */
    public record Delete(
            String bucket,
            String objectPath,
            boolean deleted
    ) {
    }
}
