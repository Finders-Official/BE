package com.finders.api.domain.store.service.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finders.api.domain.store.dto.response.PhotoLabParentRegionCountResponse;
import com.finders.api.domain.store.dto.response.PhotoLabRegionFilterCacheDTO;
import com.finders.api.domain.store.dto.response.PhotoLabRegionFilterResponse;
import com.finders.api.domain.store.dto.response.PhotoLabRegionItemResponse;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.domain.store.repository.RegionRepository;
import com.finders.api.infra.redis.RedisCacheClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoLabRegionCacheService {

    public static final String KEY = "photoLabRegionCounts::all";
    private static final long REGION_COUNTS_CACHE_TTL_MINUTES = 60L;
    private static final Duration TTL = Duration.ofMinutes(REGION_COUNTS_CACHE_TTL_MINUTES);

    private final PhotoLabRepository photoLabRepository;
    private final RegionRepository regionRepository;
    private final RedisCacheClient redisCacheClient;
    private final ObjectMapper objectMapper;

    public PhotoLabRegionFilterResponse getPhotoLabCountsByRegion() {
        return redisCacheClient.get(KEY)
                .map(this::convertOrNull)
                .filter(response -> response != null)
                .orElseGet(this::loadFromDbAndCache);
    }

    private PhotoLabRegionFilterResponse loadFromDbAndCache() {
        List<PhotoLabParentRegionCountResponse> parents = photoLabRepository.countPhotoLabsByTopRegion();
        List<PhotoLabRegionItemResponse> regions = regionRepository.findAllRegionItems();
        PhotoLabRegionFilterResponse response = new PhotoLabRegionFilterResponse(parents, regions);

        redisCacheClient.set(KEY, PhotoLabRegionFilterCacheDTO.from(response), TTL);
        return response;
    }

    @SuppressWarnings("unchecked")
    private PhotoLabRegionFilterResponse convertOrNull(Object cached) {
        try {
            if (cached instanceof PhotoLabRegionFilterCacheDTO dto) {
                return dto.toResponse();
            }

            if (cached instanceof PhotoLabRegionFilterResponse response) {
                return response;
            }

            if (cached instanceof Map<?, ?> map) {
                PhotoLabRegionFilterCacheDTO dto = objectMapper.convertValue((Map<String, Object>) map, PhotoLabRegionFilterCacheDTO.class);
                return dto.toResponse();
            }
        } catch (RuntimeException e) {
            log.warn("[PhotoLabRegionCacheService.convertOrNull] cache payload conversion failed. fallback to DB.", e);
        }

        log.warn("[PhotoLabRegionCacheService.convertOrNull] unexpected cache payload type: {}. fallback to DB.",
                cached != null ? cached.getClass().getName() : "null");
        return null;
    }
}
