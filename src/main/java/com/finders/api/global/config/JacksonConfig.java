package com.finders.api.global.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson ObjectMapper 설정
 *
 * <p>날짜/시간 직렬화 형식: ISO 8601 표준</p>
 * <ul>
 *   <li>LocalDateTime: yyyy-MM-dd'T'HH:mm:ss (예: 2025-12-23T14:30:00)</li>
 *   <li>LocalDate: yyyy-MM-dd (예: 2025-12-23)</li>
 *   <li>LocalTime: HH:mm:ss (예: 14:30:00)</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 지원 (ISO 8601 형식)
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
