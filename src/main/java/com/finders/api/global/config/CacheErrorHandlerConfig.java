package com.finders.api.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CacheErrorHandlerConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn(
                        "[CacheErrorHandler.handleCacheGetError] GET error. cache={}, key={}. fallback to DB",
                        cache != null ? cache.getName() : "unknown",
                        key,
                        exception
                );
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("[CacheErrorHandler.handleCachePutError] PUT error. cache={}, key={}",
                        cache != null ? cache.getName() : "unknown", key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("[CacheErrorHandler.handleCacheEvictError] EVICT error. cache={}, key={}",
                        cache != null ? cache.getName() : "unknown", key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("[CacheErrorHandler.handleCacheClearError] CLEAR error. cache={}",
                        cache != null ? cache.getName() : "unknown", exception);
            }
        };
    }
}