package com.finders.api.domain.store.controller;

import com.finders.api.domain.store.dto.request.PhotoLabRequest;
import com.finders.api.domain.store.dto.request.PhotoLabSearchCondition;
import com.finders.api.domain.store.dto.response.PhotoLabDetailResponse;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;
import com.finders.api.domain.store.dto.response.PhotoLabListResponse;
import com.finders.api.domain.store.dto.response.PhotoLabPopularResponse;
import com.finders.api.domain.store.dto.response.PhotoLabResponse;
import com.finders.api.domain.store.service.command.PhotoLabFavoriteCommandService;
import com.finders.api.domain.store.service.query.PhotoLabPopularQueryService;
import com.finders.api.domain.store.service.query.PhotoLabQueryService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.PagedResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PhotoLab_USER", description = "현상소 API")
@RestController
@RequestMapping("/photo-labs")
@RequiredArgsConstructor
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
                    "(파라미터 순서대로)검색어, 특징태그, 지역필터, 날짜필터를 기반으로 해당하는 현상소를 조회합니다.\n\n" +
                    "*위치 기반 알고리즘: 위치 사용 약관 동의 o -> 가까운 위치 + 작업 건수 순으로 정렬, 위치 사용 약관 동의 x -> 작업 건수 순으로 정렬")
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
            description="즐겨찾기 별 버튼\n\n" +
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
    @Operation(summary = "커뮤니티 현상소 검색",
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
