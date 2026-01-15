package com.finders.api.infra.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GCS 설정 프로퍼티 (불변 객체)
 */
@ConfigurationProperties(prefix = "google.cloud.storage")
public record StorageProperties(
        /**
         * 공개 버킷 이름 (예: finders-public)
         */
        String publicBucket,

        /**
         * 비공개 버킷 이름 (예: finders-private)
         */
        String privateBucket,

        /**
         * Signed URL 만료 시간 (분, 기본값: 60분)
         */
        Integer signedUrlExpiryMinutes,

        /**
         * Signed URL 생성에 사용할 서비스 계정 이메일 (GCE에서 IAM Signing 사용 시 필수)
         */
        String serviceAccountEmail
) {
    public StorageProperties {
        if (signedUrlExpiryMinutes == null) {
            signedUrlExpiryMinutes = 60;
        }
    }

    /**
     * Public 버킷 기본 URL 반환
     */
    public String getPublicBaseUrl() {
        return String.format("https://storage.googleapis.com/%s", publicBucket);
    }
}
