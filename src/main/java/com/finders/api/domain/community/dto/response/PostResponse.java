package com.finders.api.domain.community.dto.response;

import com.finders.api.domain.community.entity.Post;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostResponse {

    // 피드 목록 조회
    @Builder
    public record PostsResDTO(
            Long postId,
            String title,
            String imageUrl
    ) {
        public static PostsResDTO from(Post post) {
            return PostsResDTO.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .imageUrl(null)
                    .build();
        }
    }

    // 게시글 상세 조회
    @Builder
    public record PostDetailResDTO(
            Long postId,
            String profileImageUrl,
            String nickname,
            LocalDateTime createdAt,
            String title,
            String content,
            List<String> imageUrls,
            Integer likeCount,
            boolean isLiked,
            boolean isSelfDeveloped,
            boolean isMine,
            Integer commentCount,
            LabReviewResDTO labReview
    ) {
        public static PostDetailResDTO from(Post post, boolean isLiked, boolean isMine) {
            return PostDetailResDTO.builder()
                    .postId(post.getId())
                    .profileImageUrl(post.getMemberUser().getProfileImage())
                    .nickname(post.getMemberUser().getName())
                    .createdAt(post.getCreatedAt())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .imageUrls(new ArrayList<>())
                    .likeCount(post.getLikeCount())
                    .isLiked(isLiked)
                    .isSelfDeveloped(post.isSelfDeveloped())
                    .isMine(isMine)
                    .commentCount(post.getCommentCount())
                    .labReview(post.getPhotoLab() != null ? LabReviewResDTO.from(post) : null)
                    .build();
        }
    }

    // 현상소 리뷰
    @Builder
    public record LabReviewResDTO(
            Long labId,
            String labName,
            String content
    ) {
        public static LabReviewResDTO from(Post post) {
            return LabReviewResDTO.builder()
                    .labId(post.getPhotoLab().getId())
                    .labName(post.getPhotoLab().getName())
                    .content(post.getLabReview())
                    .build();
        }
    }

    // 홈페이지 사진 수다 사진 미리보기
    // 개별 항목 DTO
    @Builder
    public record PostPreviewDTO(
            Long postId,
            String imageUrl,
            String title,
            Integer likeCount,
            Integer commentCount,
            boolean isLiked
    ) {
        public static PostPreviewDTO from(Post post, boolean isLiked) {
            return PostPreviewDTO.builder()
                    .postId(post.getId())
                    .imageUrl(null)
                    .title(post.getTitle())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .isLiked(isLiked)
                    .build();
        }
    }

    // 미리보기 리스트를 감싸는 DTO
    @Builder
    public record PostPreviewListDTO(
            List<PostPreviewDTO> previewList
    ) {
        public static PostPreviewListDTO from(List<PostPreviewDTO> previewDTOs) {
            return PostPreviewListDTO.builder()
                    .previewList(previewDTOs)
                    .build();
        }
    }
}