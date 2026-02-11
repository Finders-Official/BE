package com.finders.api.domain.community.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.domain.community.dto.response.PostCacheDTO;
import com.finders.api.domain.community.repository.PostQueryRepository;
import com.finders.api.infra.redis.RedisCacheClient;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PopularPostCacheService {

    private static final String KEY = "popularPosts::home_top10";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final PostQueryRepository postQueryRepository;
    private final RedisCacheClient redisCacheClient;
    private final ObjectMapper objectMapper;

    public List<PostCacheDTO> getPopularPosts() {
        return redisCacheClient.get(KEY)
                .map(this::convert)
                .orElseGet(() -> {
                    List<PostCacheDTO> posts = postQueryRepository.findTop10PopularPosts();
                    redisCacheClient.set(KEY, posts, TTL);
                    return posts;
                });
    }

    @SuppressWarnings("unchecked")
    private List<PostCacheDTO> convert(Object cached) {
        if (!(cached instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }

        if (list.get(0) instanceof PostCacheDTO dto) {
            return (List<PostCacheDTO>) list;
        }

        return ((List<Map<String, Object>>) cached).stream()
                .map(m -> objectMapper.convertValue(m, PostCacheDTO.class))
                .toList();
    }

    public void evictPopularPosts() {
        redisCacheClient.delete(KEY);
    }

    public List<PostCacheDTO> refreshPopularPostsCache() {
        // 1. DB에서 최신 데이터 조회 (PostQueryRepository의 메서드 활용)
        List<PostCacheDTO> posts = postQueryRepository.findTop10PopularPosts();

        // 2. Redis에 덮어쓰기 (기존 데이터가 있어도 TTL과 함께 새로 저장됨)
        redisCacheClient.set(KEY, posts, TTL);

        return posts;
    }

}
