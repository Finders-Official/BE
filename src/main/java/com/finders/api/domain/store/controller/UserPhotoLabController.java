package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.request.PhotoLabSearchCondition;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;
import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabPopularResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.service.command.PhotoLabFavoriteCommandService;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.service.query.PhotoLabPopularQueryService;
import com.finders.api.domain.store.service.query.PhotoLabQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "PhotoLab_USER", description = "현상소 API")
@RestController
@RequestMapping("/photo-labs")
@RequiredArgsConstructor
public class UserPhotoLabController {
    private final PhotoLabPopularQueryService photoLabPopularQueryService;
    private final PhotoLabQueryService photoLabQueryService;
    private final PhotoLabFavoriteCommandService photoLabFavoriteCommandService;

    @Operation(summary = "인기 현상소 조회 API")
    @GetMapping("/popular")
    public ApiResponse<List<PhotoLabPopularResponse.Card>> getPopularPhotoLabs() {
        return ApiResponse.success(
                SuccessCode.OK,
                photoLabPopularQueryService.getPopularPhotoLabs()
        );
    }

    @Operation(summary = "현상소 목록 조회 API")
    @GetMapping
    public PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        Long memberId = user != null ? user.memberId() : null;
        PhotoLabSearchCondition condition = PhotoLabSearchCondition.builder()
                .memberId(memberId)
                .query(q)
                .tagIds(tagIds)
                .regionId(regionId)
                .date(date)
                .page(page)
                .size(size)
                .lat(lat)
                .lng(lng)
                .build();

        return photoLabQueryService.getPhotoLabs(condition);
    }

    @Operation(summary = "현상소 즐겨찾기 추가 API")
    @PostMapping("/{photoLabId}/favorites")
    public ApiResponse<PhotoLabFavoriteResponse.Status> addFavorite(
            @PathVariable Long photoLabId,
            @AuthenticationPrincipal AuthUser user
    ) {
        return ApiResponse.success(
                SuccessCode.OK,
                photoLabFavoriteCommandService.addFavorite(photoLabId, user != null ? user.memberId() : null)
        );
    }

    @Operation(summary = "현상소 즐겨찾기 삭제 API")
    @DeleteMapping("/{photoLabId}/favorites")
    public ApiResponse<PhotoLabFavoriteResponse.Status> removeFavorite(
            @PathVariable Long photoLabId,
            @AuthenticationPrincipal AuthUser user
    ) {
        return ApiResponse.success(
                SuccessCode.OK,
                photoLabFavoriteCommandService.removeFavorite(photoLabId, user != null ? user.memberId() : null)
        );
    }

    // 커뮤니티 현상소 검색
    @Operation(summary = "현상소 검색", description = "게시글 작성 시 연결할 현상소를 검색합니다. 위도/경도가 없으면 거리 없이 주소만 나옵니다.")
    @GetMapping("/search")
    public ApiResponse<PhotoLabResponse.PhotoLabSearchListDTO> searchLabs(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "latitude", required = false) Double latitude,
            @RequestParam(name = "longitude", required = false) Double longitude,
            @PageableDefault(size = 8) Pageable pageable
    ) {
        return ApiResponse.success(
                SuccessCode.STORE_LIST_FOUND,
                photoLabQueryService.searchPhotoLabs(keyword, latitude, longitude, pageable)
        );
    }
}
