package com.finders.api.domain.store.service.query;

import com.finders.api.domain.community.entity.PostImage;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.PostImageRepository;
import com.finders.api.domain.member.entity.FavoritePhotoLab;
import com.finders.api.domain.member.service.query.MemberQueryService;
import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.request.PhotoLabSearchCondition;
import com.finders.api.domain.store.dto.response.PhotoLabDetailResponse;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;
import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.entity.PhotoLabImage;
import com.finders.api.domain.store.entity.PhotoLabTag;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.finders.api.domain.store.repository.PhotoLabFavoriteRepository;
import com.finders.api.domain.store.repository.PhotoLabImageRepository;
import com.finders.api.domain.store.repository.PhotoLabNoticeRepository;
import com.finders.api.domain.store.repository.PhotoLabQueryRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.domain.store.repository.PhotoLabTagQueryRepository;
import com.finders.api.domain.terms.enums.TermsType;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabQueryServiceImpl implements PhotoLabQueryService {

    private static final int POST_IMAGE_LIMIT = 10;
    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final PhotoLabQueryRepository photoLabQueryRepository;
    private final PhotoLabImageRepository photoLabImageRepository;
    private final PhotoLabTagQueryRepository photoLabTagQueryRepository;
    private final PhotoLabFavoriteRepository photoLabFavoriteRepository;
    private final PhotoLabNoticeRepository photoLabNoticeRepository;
    private final PostImageRepository postImageRepository;
    private final MemberQueryService memberQueryService;
    private final StorageService storageService;

    // 커뮤니티 현상소 검색 관련
    private final PhotoLabRepository photoLabRepository;
    private static final String DISTANCE_FORMAT_KM = "%.1fkm";

    @Override
    public PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(PhotoLabSearchCondition condition) {
        int pageNumber = (condition.page() != null && condition.page() >= 0) ? condition.page() : 0;
        int pageSize = (condition.size() != null && condition.size() > 0) ? condition.size() : 20;

        boolean useDistance = shouldUseDistance(condition.memberId(), condition.lat(), condition.lng());

        Page<PhotoLab> photoLabPage = photoLabQueryRepository.search(
                condition.query(),
                condition.tagIds(),
                condition.regionId(),
                condition.date(),
                pageNumber,
                pageSize,
                condition.lat(),
                condition.lng(),
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
        Set<Long> favoriteLabIds = buildFavoriteSet(condition.memberId(), photoLabIds);

        List<PhotoLabListResponse.Card> cards = photoLabPage.getContent().stream()
                .map(photoLab -> PhotoLabListResponse.Card.from(
                        photoLab,
                        imageUrlsByLabId.getOrDefault(photoLab.getId(), List.of()),
                        tagsByLabId.getOrDefault(photoLab.getId(), List.of()),
                        distanceKmOrNull(condition.lat(), condition.lng(), photoLab),
                        favoriteLabIds.contains(photoLab.getId())
                ))
                .toList();

        return PagedResponse.of(SuccessCode.STORE_LIST_FOUND, cards, photoLabPage);
    }

    @Override
    public PhotoLabDetailResponse.Detail getPhotoLabDetail(Long photoLabId, Long memberId, Double lat, Double lng) {
        PhotoLab photoLab = photoLabRepository.findByIdAndStatus(photoLabId, PhotoLabStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        boolean isFavorite = memberId != null &&
                photoLabFavoriteRepository.existsByMember_IdAndPhotoLab_Id(memberId, photoLabId);

        Double distanceKm = null;
        if (shouldUseDistance(memberId, lat, lng)) {
            distanceKm = distanceKmOrNull(lat, lng, photoLab);
        }

        PhotoLabDetailResponse.Notice notice = photoLabNoticeRepository
                .findFirstByPhotoLab_IdAndIsActiveTrueOrderByCreatedAtDesc(photoLabId)
                .map(item -> new PhotoLabDetailResponse.Notice(item.getNoticeType(), item.getTitle()))
                .orElse(null);

        return PhotoLabDetailResponse.Detail.builder()
                .photoLabId(photoLab.getId())
                .name(photoLab.getName())
                .imageUrls(buildImageUrls(photoLabId))
                .tags(buildTags(photoLabId))
                .address(photoLab.getAddress())
                .addressDetail(photoLab.getAddressDetail())
                .distanceKm(distanceKm)
                .isFavorite(isFavorite)
                .workCount(photoLab.getWorkCount())
                .reviewCount(photoLab.getReviewCount())
                .avgWorkTime(photoLab.getAvgWorkTime())
                .mainNotice(notice)
                .postImageUrls(buildPostImageUrls(photoLabId))
                .build();
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
                    .add(image.getObjectPath());
        }
        return result;
    }

    private List<String> buildImageUrls(Long photoLabId) {
        Map<Long, List<String>> imageUrlMap = buildImageUrlMap(List.of(photoLabId));
        return imageUrlMap.getOrDefault(photoLabId, List.of());
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

    private List<String> buildTags(Long photoLabId) {
        Map<Long, List<String>> tagMap = buildTagMap(List.of(photoLabId));
        return tagMap.getOrDefault(photoLabId, List.of());
    }

    private Set<Long> buildFavoriteSet(Long memberId, List<Long> photoLabIds) {
        if (memberId == null || photoLabIds == null || photoLabIds.isEmpty()) {
            return Set.of();
        }
        List<Long> favoriteIds = photoLabFavoriteRepository.findFavoritePhotoLabIds(memberId, photoLabIds);
        return Set.copyOf(favoriteIds);
    }

    private List<String> buildPostImageUrls(Long photoLabId) {
        List<PostImage> postImages = postImageRepository.findByPhotoLabIdAndPostStatus(
                photoLabId,
                CommunityStatus.ACTIVE,
                PageRequest.of(0, POST_IMAGE_LIMIT)
        );
        if (postImages.isEmpty()) {
            return List.of();
        }

        List<String> keys = postImages.stream()
                .map(PostImage::getObjectPath)
                .toList();

        Map<String, StorageResponse.SignedUrl> signedMap = storageService.getSignedUrls(keys, SIGNED_URL_EXPIRY_MINUTES);

        return keys.stream()
                .map(key -> {
                    StorageResponse.SignedUrl signedUrl = signedMap.get(key);
                    return signedUrl != null ? signedUrl.url() : null;
                })
                .filter(Objects::nonNull)
                .toList();
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
        return memberQueryService.hasAgreedToTerms(memberId, TermsType.LOCATION);
    }

    // 커뮤니티 현상소 검색
    @Override
    public PhotoLabResponse.PhotoLabSearchListDTO searchCommunityPhotoLabs(
            PhotoLabRequest.PhotoLabCommunitySearchRequest request
    ) {
        Double lat = request.locationAgreed() ? request.latitude() : 0.0;
        Double lng = request.locationAgreed() ? request.longitude() : 0.0;

        List<PhotoLabRepository.PhotoLabSearchResult> searchResults = photoLabRepository.searchCommunityPhotoLabs(
                request.keyword(),
                lat,
                lng,
                request.locationAgreed()
        );

        List<PhotoLabResponse.PhotoLabSearchDTO> dtos = searchResults.stream()
                .map(result -> {
                    String distanceStr = null;

                    if (request.locationAgreed() && result.getDistanceVal() != null) {
                        double distanceKm = result.getDistanceVal() / 1000.0;
                        distanceStr = String.format(DISTANCE_FORMAT_KM, distanceKm);
                    }

                    return PhotoLabResponse.PhotoLabSearchDTO.from(
                            result.getId(),
                            result.getName(),
                            result.getAddress(),
                            distanceStr,
                            request.locationAgreed()
                    );
                })
                .toList();

        return PhotoLabResponse.PhotoLabSearchListDTO.from(dtos);
    }

    @Override
    public PhotoLabFavoriteResponse.SliceResponse getFavoritePhotoLabs(Long memberId, int page, int size, Double lat, Double lng) {
        if (page < 0 || size <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        Pageable pageable = PageRequest.of(page, size);

        Slice<FavoritePhotoLab> favoriteSlice = photoLabFavoriteRepository.findSliceByMember_Id(memberId, pageable);

        if (favoriteSlice.isEmpty()) {
            return new PhotoLabFavoriteResponse.SliceResponse(
                    Collections.emptyList(),
                    new PhotoLabFavoriteResponse.PageInfo(page, size, true)
            );
        }

        List<PhotoLab> photoLabs = favoriteSlice.getContent().stream()
                .map(FavoritePhotoLab::getPhotoLab)
                .toList();
        List<Long> photoLabIds = photoLabs.stream()
                .map(PhotoLab::getId)
                .toList();

        Map<Long, List<String>> imageUrlsByLabId = buildImageUrlMap(photoLabIds);
        Map<Long, List<String>> tagsByLabId = buildTagMap(photoLabIds);

        boolean useDistance = shouldUseDistance(memberId, lat, lng);

        List<PhotoLabListResponse.Card> cards = favoriteSlice.getContent().stream()
                .map(f -> {
                    PhotoLab favoritePhotoLab = f.getPhotoLab();

                    Double distanceKm = useDistance ? distanceKmOrNull(lat, lng, favoritePhotoLab) : null;

                    return PhotoLabListResponse.Card.from(
                            favoritePhotoLab,
                            imageUrlsByLabId.getOrDefault(favoritePhotoLab.getId(), Collections.emptyList()),
                            tagsByLabId.getOrDefault(favoritePhotoLab.getId(), Collections.emptyList()),
                            distanceKm,
                            true
                    );
                })
                .toList();

        return new PhotoLabFavoriteResponse.SliceResponse(
                cards,
                new PhotoLabFavoriteResponse.PageInfo(
                        favoriteSlice.getNumber(),
                        favoriteSlice.getSize(),
                        favoriteSlice.isLast()
                )
        );
    }

}
