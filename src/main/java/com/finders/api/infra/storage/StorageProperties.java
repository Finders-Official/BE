package com.finders.api.infra.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GCS 설정 프로퍼티
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "google.cloud.storage")
public class StorageProperties {

    /**
     * 공개 버킷 이름 (예: finders-public)
     */
    private String publicBucket;

    /**
     * 비공개 버킷 이름 (예: finders-private)
     */
    private String privateBucket;

    /**
     * Signed URL 만료 시간 (분, 기본값: 60분)
     */
    private int signedUrlExpiryMinutes = 60;

    /**
     * Public 버킷 기본 URL 반환
     */
    public String getPublicBaseUrl() {
        return String.format("https://storage.googleapis.com/%s", publicBucket);
    }
}
