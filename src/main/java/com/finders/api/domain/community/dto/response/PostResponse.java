package com.finders.api.domain.community.dto.response;

import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostImage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    // 공통 이미지 DTO
    public record PostImageResDTO(
            String imageUrl,
            Integer width,
            Integer height
    ) {
        public static PostImageResDTO from(PostImage image, String imageUrl) {
            return new PostImageResDTO(imageUrl, image.getWidth(), image.getHeight());
        }
    }

    // 피드 목록 조회
    public record PostsResDTO(
            Long postId,
            String title,
            PostImageResDTO image
    ) {
        public static PostsResDTO from(Post post, PostImageResDTO imageResDTO) {
            return new PostsResDTO(post.getId(), post.getTitle(), imageResDTO);
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
            List<PostImageResDTO> images,
            Integer likeCount,
            boolean isLiked,
            boolean isSelfDeveloped,
            boolean isMine,
            Integer commentCount,
            LabReviewResDTO labReview
    ) {
        public static PostDetailResDTO fromNewPost(Post post, String profileImageUrl, List<PostImageResDTO> images) {
            return PostDetailResDTO.from(
                    post,
                    false,
                    true,
                    profileImageUrl,
                    images
            );
        }

        public static PostDetailResDTO from(Post post, boolean isLiked, boolean isMine, String profileImageUrl, List<PostImageResDTO> images) {
            return PostDetailResDTO.builder()
                    .postId(post.getId())
                    .profileImageUrl(profileImageUrl)
                    .nickname(post.getMemberUser().getName())
                    .createdAt(post.getCreatedAt())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .images(images)
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
    public record LabReviewResDTO(
            Long labId,
            String labName,
            String content
    ) {
        public static LabReviewResDTO from(Post post) {
            return new LabReviewResDTO(
                    post.getPhotoLab().getId(),
                    post.getPhotoLab().getName(),
                    post.getLabReview()
            );
        }
    }

    // 홈페이지 사진 수다 사진 미리보기
    // 개별 항목 DTO
    @Builder
    public record PostPreviewDTO(
            Long postId,
            PostImageResDTO image,
            String title,
            Integer likeCount,
            Integer commentCount,
            boolean isLiked
    ) {
        public static PostPreviewDTO from(Post post, boolean isLiked, PostImageResDTO imageResDTO) {
            return PostPreviewDTO.builder()
                    .postId(post.getId())
                    .image(imageResDTO)
                    .title(post.getTitle())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .isLiked(isLiked)
                    .build();
        }
    }

    // 미리보기 리스트를 감싸는 DTO
    public record PostPreviewListDTO(
            List<PostPreviewDTO> previewList,
            Long totalCount,
            boolean isLast
    ) {
        public static PostPreviewListDTO from(List<PostPreviewDTO> previewDTOs, Long totalCount, boolean isLast) {
            return new PostPreviewListDTO(previewDTOs, totalCount, isLast);
        }

        public static PostPreviewListDTO from(List<PostPreviewDTO> previewDTOs) {
            return new PostPreviewListDTO(previewDTOs, (long) previewDTOs.size(), true);
        }
    }

    // 찜 목록 조회 관련
    @Builder
    public record PostsLikesDTO(
            Long postId,
            String title,
            PostResponse.PostImageResDTO image,
            boolean isLiked
    ) {
        public static PostsLikesDTO from(Post post, PostResponse.PostImageResDTO imageResDTO, boolean isLiked) {
            return PostsLikesDTO.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .image(imageResDTO)
                    .isLiked(isLiked)
                    .build();
        }
    }
}