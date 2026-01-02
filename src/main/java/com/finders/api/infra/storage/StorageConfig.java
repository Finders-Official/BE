package com.finders.api.infra.storage;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GCS Storage 설정
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    /**
     * Storage 클라이언트 빈 (ADC 사용)
     * <p>
     * - 로컬: gcloud auth application-default login 필요
     * - GCP 서버: 메타데이터 서버에서 자동 인증
     */
    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
