package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.request.PhotoLabSearchCondition;
import com.finders.api.domain.store.dto.response.PhotoLabDetailResponse;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;
import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabPopularResponse;
import com.finders.api.domain.store.dto.response.PhotoLabPreviewResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.dto.response.PhotoLabRegionFilterResponse;
import com.finders.api.domain.store.service.command.PhotoLabFavoriteCommandService;
import com.finders.api.domain.store.service.query.PhotoLabPopularQueryService;
import com.finders.api.domain.store.service.query.PhotoLabQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Tag(name = "PhotoLab_USER", description = "현상소 API")
@RestController
@RequestMapping("/photo-labs")
@RequiredArgsConstructor
@Validated
public class UserPhotoLabController {
    private final PhotoLabPopularQueryService photoLabPopularQueryService;
    private final PhotoLabQueryService photoLabQueryService;
    private final PhotoLabFavoriteCommandService photoLabFavoriteCommandService;

    @Operation(
            summary = "인기 현상소 조회 API",
            description = "HM-010, PL-011-1\n\n" +
                    "인기 현상소를 1위부터 8위까지 조회합니다.")
    @GetMapping("/popular")
    public ApiResponse<List<PhotoLabPopularResponse.Card>> getPopularPhotoLabs() {
        return ApiResponse.success(
                SuccessCode.OK,
                photoLabPopularQueryService.getPopularPhotoLabs()
        );
    }

    @Operation(
            summary = "현상소 목록 조회 API",
            description = "PL-010, PL-011-3\n\n" +
                    "검색어, 태그, 지역, 날짜 필터를 기반으로 현상소를 검색합니다.\n\n" +
                    "정렬 기준: 위치 약관에 동의한 경우 -> 가까운 순 정렬 후 작업 수 기준 정렬 / 동의하지 않은 경우 -> 작업 수 기준 정렬만 적용됩니다.\n\n" +
                    "[지역 필터]\n" +
                    "- parentRegionId만 전달된 경우: 해당 상위 지역에 속한 모든 하위 지역을 포함합니다.\n" +
                    "- parentRegionId + regionIds가 함께 전달된 경우: regionIds에 포함된 하위 지역만 필터링합니다. (선택된 하위 지역들만 적용)\n")
    @GetMapping
    public PagedResponse<PhotoLabListResponse.Card> getPhotoLabs(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Long parentRegionId,
            @RequestParam(required = false) List<Long> regionIds,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.TIME) LocalTime time,
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
                .parentRegionId(parentRegionId)
                .regionIds(regionIds)
                .date(date)
                .time(time)
                .page(page)
                .size(size)
                .lat(lat)
                .lng(lng)
                .build();

        return photoLabQueryService.getPhotoLabs(condition);
    }

    @Operation(
            summary = "현상소 목록 조회 preview API",
            description = "PL-011-2\n\n" +
                    "검색창 UI를 위한 가벼운 결과 미리보기 검색 API입니다.(현상소 목록 조회와 동일한 로직.)")
    @GetMapping("/search/preview")
    public PagedResponse<PhotoLabPreviewResponse.Card> getPhotoLabsPreview(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Long parentRegionId,
            @RequestParam(required = false) List<Long> regionIds,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.TIME) LocalTime time,
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
                .parentRegionId(parentRegionId)
                .regionIds(regionIds)
                .date(date)
                .time(time)
                .page(page)
                .size(size)
                .lat(lat)
                .lng(lng)
                .build();

        return photoLabQueryService.getPhotoLabsPreview(condition);
    }

    @Operation(
            summary = "현상소 상세 조회 API",
            description = "PL-020\n\n" +
                    "특정 현상소에 대한 상세페이지를 조회합니다.")
    @GetMapping("/{photoLabId}")
    public ApiResponse<PhotoLabDetailResponse.Detail> getPhotoLabDetail(
            @PathVariable Long photoLabId,
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        Long memberId = user != null ? user.memberId() : null;
        return ApiResponse.success(
                SuccessCode.STORE_FOUND,
                photoLabQueryService.getPhotoLabDetail(photoLabId, memberId, lat, lng)
        );
    }

    @Operation(
            summary = "현상소 즐겨찾기 추가 API",
            description = "즐겨찾기 별 버튼\n\n" +
                    "특정 현상소를 관심 현상소로 등록합니다.")
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

    @Operation(
            summary = "현상소 즐겨찾기 삭제 API",
            description = "즐겨찾기 별 버튼\n\n" +
                    "특정 현상소를 관심 현상소에서 삭제합니다.")
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
    @Operation(summary = "커뮤니티용 현상소 검색",
            description = "게시글 작성 시 연결할 현상소를 검색합니다. 1순위 정확도 순 > 2순위 거리 순 + 예약 수로 정렬됩니다. 위치 정보 미동의 시 예약 수로 정렬되며 주소만 노출됩니다.")
    @GetMapping("/search")
    public ApiResponse<PhotoLabResponse.PhotoLabSearchListDTO> searchLabs(
            @ModelAttribute PhotoLabRequest.PhotoLabCommunitySearchRequest request
    ) {
        return ApiResponse.success(
                SuccessCode.STORE_LIST_FOUND,
                photoLabQueryService.searchCommunityPhotoLabs(request)
        );
    }

    @Operation(
            summary = "현상소 검색어 자동완성 API",
            description = "PL-011-2\n\n" +
                    "현상소 검색어 자동완성\n\n" +
                    "입력된 검색어와 정확히 일치하거나 부분적으로 일치하는 검색어를 보여주는 목록 (최대 4개)")
    @GetMapping("/search/autocomplete")
    public ApiResponse<List<String>> autocomplete(
            @RequestParam(name = "keyword") @NotBlank String keyword
    ) {
        return ApiResponse.success(
                SuccessCode.OK,
                photoLabQueryService.autocompletePhotoLabNames(keyword)
        );
    }

    @Operation(
            summary = "지역별 현상소 개수 조회 API",
            description = "PL-010\n\n" +
                    "지역을 조회하고, 시/도 별 현상소 개수를 조회합니다.")
    @GetMapping("/region")
    public ApiResponse<PhotoLabRegionFilterResponse> getPhotoLabCountsByRegion() {
        return ApiResponse.success(
                SuccessCode.STORE_LIST_FOUND,
                photoLabQueryService.getPhotoLabCountsByRegion()
        );
    }

    @Operation(
            summary = "관심 현상소 목록 조회(무한 스크롤)",
            description = "사용자가 관심 등록한 현상소 목록을 최근 등록순으로 조회합니다.\n\n" +
                    "### [주요 기능]\n" +
                    "- **무한 스크롤**: Slice 방식을 사용하여 전체 페이지 수 대신 '다음 페이지 존재 여부(hasNext)'를 반환합니다.\n" +
                    "- **최신순 정렬**: 가장 최근에 관심 등록한 현상소가 목록 상단에 노출됩니다.\n" +
                    "- **거리 계산**: 위도(lat)와 경도(lng)를 쿼리 파라미터로 전달하면 각 현상소와의 거리를 계산하여 반환합니다. (단, 사용자의 위치 정보 활용 약관 동의가 필요하며 미동의 시 distance는 null로 반환됩니다." +
                    "- sort 부분은 무시하셔도 됩니다.)"
    )
    @GetMapping("/favorites")
    public ApiResponse<PhotoLabFavoriteResponse.SliceResponse> getFavoritePhotoLabs(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        PhotoLabFavoriteResponse.SliceResponse response = photoLabQueryService.getFavoritePhotoLabs(
                authUser.memberId(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                lat,
                lng
        );
        return ApiResponse.success(SuccessCode.STORE_FAVORITE_LIST_FOUND, response);
    }

}
