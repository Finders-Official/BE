package com.finders.api.domain.community.service;

import com.finders.api.domain.community.repository.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularPostCacheService {

    private final PostQueryRepository postQueryRepository;

    @Cacheable(value = "popularPosts", key = "'home_top10'")
    public List<?> getPopularPosts() {
        return postQueryRepository.findTop10PopularPosts();
    }
}
