package com.finders.api.domain.community.controller;

import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community", description = "사진 수다 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    // 게시글 관련
    @Operation(summary = "피드 목록 조회", description = "전체 게시글 목록을 최신순으로 조회합니다.")
    @GetMapping("/posts")
    public ApiResponse<String> getPosts() {
        return ApiResponse.success(SuccessCode.POST_FOUND, "피드 목록 조회 성공");
    }

    @Operation(summary = "게시물 작성", description = "게시글 등록 API입니다.")
    @PostMapping("/posts")
    public ApiResponse<String> createPost() {
        return ApiResponse.success(SuccessCode.POST_CREATED, "게시물 작성 성공");
    }

    @Operation(summary = "게시물 상세 조회", description = "특정 ID의 게시글 상세 정보를 조회합니다.")
    @GetMapping("/posts/{postId}")
    public ApiResponse<String> getPostDetail(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.POST_FOUND, postId + "번 게시물 상세 조회 성공");
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/posts/{postId}")
    public ApiResponse<String> deletePost(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.OK, postId + "번 게시글 삭제 성공");
    }

    // 댓글 관련
    @Operation(summary = "게시물 댓글 조회", description = "특정 게시글에 달린 댓글 목록을 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<String> getComments(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.OK, postId + "번 게시물의 댓글 목록 조회 성공");
    }

    @Operation(summary = "게시물 댓글 작성", description = "특정 게시글에 새로운 댓글을 남깁니다.")
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<String> createComment(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.CREATED, postId + "번 게시물에 댓글 작성 성공");
    }

    @Operation(summary = "게시물 댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable Long commentId) {
        return ApiResponse.success(SuccessCode.OK, commentId + "번 댓글 삭제 성공");
    }

    // 좋아요 관련
    @Operation(summary = "게시물 좋아요", description = "게시글에 좋아요를 누릅니다.")
    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<String> addLike(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.OK, postId + "번 게시물 좋아요 성공");
    }

    @Operation(summary = "게시물 좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    @DeleteMapping("/posts/{postId}/likes")
    public ApiResponse<String> cancelLike(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.OK, postId + "번 게시물 좋아요 취소 성공");
    }

    // 현상소 관련
    @Operation(summary = "현상소 검색", description = "게시글 작성 시 연결할 현상소를 검색합니다.")
    @GetMapping("/labs")
    public ApiResponse<String> searchLabs(@RequestParam(required = false) String query) {
        return ApiResponse.success(SuccessCode.STORE_LIST_FOUND, "현상소 검색 성공: " + query);
    }
}