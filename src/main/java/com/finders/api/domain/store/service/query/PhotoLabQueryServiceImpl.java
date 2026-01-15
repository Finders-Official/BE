package com.finders.api.domain.store.service.query;

import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.entity.PhotoLabImage;
import com.finders.api.domain.store.entity.PhotoLabTag;
import com.finders.api.domain.member.repository.MemberAgreementRepository;
import com.finders.api.domain.store.repository.FavoritePhotoLabRepository;
import com.finders.api.domain.store.repository.PhotoLabImageRepository;
import com.finders.api.domain.store.repository.PhotoLabTagQueryRepository;
import com.finders.api.domain.store.repository.PhotoLabQueryRepository;
import com.finders.api.domain.terms.enums.TermsType;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabQueryServiceImpl implements PhotoLabQueryService {

    private final PhotoLabQueryRepository photoLabQueryRepository;
    private final PhotoLabImageRepository photoLabImageRepository;
    private final PhotoLabTagQueryRepository photoLabTagQueryRepository;
    private final FavoritePhotoLabRepository favoritePhotoLabRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final StorageService storageService;

    // 커뮤니티 현상소 검색 관련
    private final PhotoLabRepository photoLabRepository;
    private static final String DISTANCE_FORMAT_KM = "%.1fkm";
    private static final int MINUTES_IN_DEGREE = 60;
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.1515;
    private static final double KILOMETERS_PER_STATUTE_MILE = 1.609344;

    @Override
    public PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(
            Long memberId,
            String query,
            List<Long> tagIds,
            Long regionId,
            LocalDate date,
            Integer page,
            Integer size,
            Double lat,
            Double lng
    ) {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;

        boolean useDistance = shouldUseDistance(memberId, lat, lng);

        Page<PhotoLab> photoLabPage = photoLabQueryRepository.search(
                query,
                tagIds,
                regionId,
                date,
                pageNumber,
                pageSize,
                lat,
                lng,
                useDistance
        );

        if (photoLabPage.isEmpty()) {
            return PagedResponse.of(SuccessCode.STORE_LIST_FOUND, List.of(), photoLabPage);
        }

        List<Long> photoLabIds = photoLabPage.getContent().stream()
                .map(PhotoLab::getId)
                .toList();

        Map<Long, List<String>> imageUrlsByLabId = buildImageUrlMap(photoLabIds);
        Map<Long, List<String>> tagsByLabId = buildTagMap(photoLabIds);
        Set<Long> favoriteLabIds = buildFavoriteSet(memberId, photoLabIds);

        List<PhotoLabListResponse.Card> cards = photoLabPage.getContent().stream()
                .map(photoLab -> PhotoLabListResponse.Card.builder()
                        .photoLabId(photoLab.getId())
                        .name(photoLab.getName())
                        .imageUrls(imageUrlsByLabId.getOrDefault(photoLab.getId(), List.of()))
                        .tags(tagsByLabId.getOrDefault(photoLab.getId(), List.of()))
                        .address(photoLab.getAddress())
                        .distanceKm(distanceKmOrNull(lat, lng, photoLab))
                        .isFavorite(favoriteLabIds.contains(photoLab.getId()))
                        .workCount(photoLab.getWorkCount())
                        .avgWorkTime(photoLab.getAvgWorkTime())
                        .build())
                .toList();

        return PagedResponse.of(SuccessCode.STORE_LIST_FOUND, cards, photoLabPage);
    }

    private Map<Long, List<String>> buildImageUrlMap(List<Long> photoLabIds) {
        List<PhotoLabImage> images = photoLabImageRepository.findByPhotoLabIds(photoLabIds);
        if (images.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (PhotoLabImage image : images) {
            Long photoLabId = image.getPhotoLab().getId();
            result.computeIfAbsent(photoLabId, key -> new java.util.ArrayList<>())
                    .add(storageService.getPublicUrl(image.getImageUrl()));
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

    private Set<Long> buildFavoriteSet(Long memberId, List<Long> photoLabIds) {
        if (memberId == null || photoLabIds == null || photoLabIds.isEmpty()) {
            return Set.of();
        }
        List<Long> favoriteIds = favoritePhotoLabRepository.findFavoritePhotoLabIds(memberId, photoLabIds);
        return Set.copyOf(favoriteIds);
    }

    private Double distanceKmOrNull(Double lat, Double lng, PhotoLab photoLab) {
        if (lat == null || lng == null || photoLab.getLatitude() == null || photoLab.getLongitude() == null) {
            return null;
        }
        double labLat = photoLab.getLatitude().doubleValue();
        double labLng = photoLab.getLongitude().doubleValue();
        return haversineKm(lat, lng, labLat, labLng);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }

    private boolean shouldUseDistance(Long memberId, Double lat, Double lng) {
        if (lat == null || lng == null || memberId == null) {
            return false;
        }
        return memberAgreementRepository.existsByMember_IdAndTerms_TypeAndIsAgreed(memberId, TermsType.LOCATION, true);
    }

    // 커뮤니티 현상소 검색
    @Override
    public PhotoLabResponse.PhotoLabSearchListDTO searchPhotoLabs(String keyword, Double latitude, Double longitude, Pageable pageable) {
        Page<PhotoLab> labs = photoLabRepository.searchByName(keyword, pageable);

        List<PhotoLabResponse.PhotoLabSearchDTO> dtos = labs.getContent().stream()
                .map(lab -> {
                    String distanceStr = null;

                    if (latitude != null && longitude != null && lab.getLatitude() != null && lab.getLongitude() != null) {
                        double distance = calculateDistance(
                                latitude,
                                longitude,
                                lab.getLatitude().doubleValue(),
                                lab.getLongitude().doubleValue()
                        );
                        distanceStr = String.format(DISTANCE_FORMAT_KM, distance);
                    }

                    return PhotoLabResponse.PhotoLabSearchDTO.from(lab, distanceStr);
                })
                .toList();

        return PhotoLabResponse.PhotoLabSearchListDTO.from(dtos);
    }

    // 현상소 검색 직선 거리 계산
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * MINUTES_IN_DEGREE * STATUTE_MILES_PER_NAUTICAL_MILE * KILOMETERS_PER_STATUTE_MILE;
    }
}
