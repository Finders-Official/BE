package com.finders.api.domain.store.service.query;

import com.finders.api.domain.store.dto.response.PhotoLabPopularResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.entity.PhotoLabImage;
import com.finders.api.domain.store.entity.PhotoLabTag;
import com.finders.api.domain.store.repository.PhotoLabImageRepository;
import com.finders.api.domain.store.repository.PhotoLabTagQueryRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.config.RedisConfig;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabPopularQueryServiceImpl implements PhotoLabPopularQueryService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabImageRepository photoLabImageRepository;
    private final PhotoLabTagQueryRepository photoLabTagQueryRepository;
    private final StorageService storageService;

    @Override
    @Cacheable(value = RedisConfig.POPULAR_PHOTO_LABS_CACHE, key = "'top8'")
    public List<PhotoLabPopularResponse.Card> getPopularPhotoLabs() {
        List<PhotoLab> photoLabs = photoLabRepository.findTop8ByOrderByReservationCountDescIdAsc();
        if (photoLabs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> photoLabIds = photoLabs.stream()
                .map(PhotoLab::getId)
                .toList();

        Map<Long, String> mainImageUrlByPhotoLabId = buildMainImageUrlMap(photoLabIds);
        Map<Long, List<String>> tagsByPhotoLabId = buildTagMap(photoLabIds);

        return photoLabs.stream()
                .map(photoLab -> PhotoLabPopularResponse.Card.from(
                        photoLab,
                        mainImageUrlByPhotoLabId.get(photoLab.getId()),
                        tagsByPhotoLabId.getOrDefault(photoLab.getId(), List.of())
                ))
                .toList();
    }

    private Map<Long, String> buildMainImageUrlMap(List<Long> photoLabIds) {
        List<PhotoLabImage> mainImages = photoLabImageRepository.findMainImagesByPhotoLabIds(photoLabIds);
        if (mainImages.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> result = new HashMap<>();
        for (PhotoLabImage image : mainImages) {
            Long photoLabId = image.getPhotoLab().getId();
            if (result.containsKey(photoLabId)) {
                continue;
            }
            result.put(photoLabId, storageService.getPublicUrl(image.getObjectPath()));
        }
        return result;
    }

    private Map<Long, List<String>> buildTagMap(List<Long> photoLabIds) {
        List<PhotoLabTag> tags = photoLabTagQueryRepository.findByPhotoLabIds(photoLabIds);
        if (tags.isEmpty()) {
            return Collections.emptyMap();
        }

        return tags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getPhotoLab().getId(),
                        Collectors.mapping(item -> item.getTag().getName(), Collectors.toList())
                ));
    }
}
