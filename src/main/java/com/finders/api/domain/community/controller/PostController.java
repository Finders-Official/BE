package com.finders.api.domain.community.controller;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.dto.response.CommentResponse;
import com.finders.api.domain.community.dto.response.PostLikeResponse;
import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.service.command.CommentCommandService;
import com.finders.api.domain.community.service.command.PostCommandService;
import com.finders.api.domain.community.service.command.PostLikeCommandService;
import com.finders.api.domain.community.service.query.CommentQueryService;
import com.finders.api.domain.community.service.query.PostQueryService;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Community", description = "사진 수다 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;
    private final PostLikeCommandService postLikeCommandService;

    // 게시글 관련
    @Operation(summary = "피드 목록 조회")
    @GetMapping
    public ApiResponse<PostResponse.PostPreviewListDTO> getPosts(@RequestParam(defaultValue = "0") Integer page) {
        return ApiResponse.success(SuccessCode.POST_FOUND, postQueryService.getPostList(page));
    }

    @Operation(summary = "게시물 작성", description = "게시글 등록 API입니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> createPost(
            @AuthenticationPrincipal MemberUser memberUser,
            @ModelAttribute @Valid PostRequest.CreatePostDTO request
    ) {
        return ApiResponse.success(SuccessCode.POST_CREATED, postCommandService.createPost(request, memberUser));
    }

    @Operation(summary = "게시물 상세 조회", description = "특정 ID의 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse.PostDetailResDTO> getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        return ApiResponse.success(SuccessCode.POST_FOUND, postQueryService.getPostDetail(postId, memberUser));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        postCommandService.deletePost(postId, memberUser);
        return ApiResponse.success(SuccessCode.OK, null);
    }

    // 댓글 관련
    @Operation(summary = "게시물 댓글 조회", description = "특정 게시글에 달린 댓글 목록을 조회합니다.")
    @GetMapping("/{postId}/comments")
    public ApiResponse<CommentResponse.CommentListDTO> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        return ApiResponse.success(SuccessCode.OK, commentQueryService.getCommentsByPost(postId, memberUser));
    }

    @Operation(summary = "게시물 댓글 작성", description = "특정 게시글에 새로운 댓글을 남깁니다.")
    @PostMapping("/{postId}/comments")
    public ApiResponse<Long> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberUser memberUser,
            @Valid @RequestBody PostRequest.CreateCommentDTO request
    ) {
        return ApiResponse.success(SuccessCode.CREATED, commentCommandService.createComment(postId, request, memberUser));
    }

    @Operation(summary = "게시물 댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        commentCommandService.deleteComment(commentId, memberUser);
        return ApiResponse.success(SuccessCode.OK, null);
    }

    // 좋아요 관련
    @Operation(summary = "게시물 좋아요", description = "게시글에 좋아요를 누릅니다.")
    @PostMapping("/{postId}/likes")
    public ApiResponse<PostLikeResponse.PostLikeResDTO> addLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        return ApiResponse.success(SuccessCode.OK, postLikeCommandService.createPostLike(postId, memberUser));
    }

    @Operation(summary = "게시물 좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    @DeleteMapping("/{postId}/likes")
    public ApiResponse<PostLikeResponse.PostLikeResDTO> cancelLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        return ApiResponse.success(SuccessCode.OK, postLikeCommandService.deletePostLike(postId, memberUser));
    }

    // HM-010 커뮤니티 사진 미리 보기
    @Operation(summary = "커뮤니티 사진 미리 보기", description = "메인 페이지에서 인기 게시물 10개를 조회합니다.")
    @GetMapping("/preview")
    public ApiResponse<PostResponse.PostPreviewListDTO> getPopularPosts(
            @AuthenticationPrincipal MemberUser memberUser
    ) {
        return ApiResponse.success(SuccessCode.POST_FOUND, postQueryService.getPopularPosts(memberUser));
    }

//    // 현상소 관련
//    @Operation(summary = "현상소 검색", description = "게시글 작성 시 연결할 현상소를 검색합니다.")
//    @GetMapping("/labs")
//    public ApiResponse<String> searchLabs(@RequestParam(required = false) String query) {
//        return ApiResponse.success(SuccessCode.STORE_LIST_FOUND, "현상소 검색 성공: " + query);
//    }
}