package com.finders.api.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<Object> get(String key) {
        try {
            return Optional.ofNullable(
                    redisTemplate.opsForValue().get(key)
            );
        } catch (RuntimeException e) {
            log.warn("[RedisCacheClient.get] Redis GET command failed for key: {}. Fallback to DB.", key, e);
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException e) {
            log.warn("[RedisCacheClient.set] Redis SET command failed for key: {}. This operation is best-effort.", key, e);
        }
    }
}