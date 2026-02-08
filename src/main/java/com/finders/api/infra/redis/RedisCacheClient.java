package com.finders.api.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisCacheClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<Object> get(String key) {
        try {
            return Optional.ofNullable(
                    redisTemplate.opsForValue().get(key)
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            // best-effort
        }
    }
}