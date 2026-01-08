package com.finders.api.domain.community.converter;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.member.entity.Member;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PostConverter {

    public static Post toPost(PostRequest.CreatePostDTO request, Member member) {
        return Post.builder()
                .title(request.title())
                .content(request.content())
                .isSelfDeveloped(request.isSelfDeveloped())
                .labReview(request.reviewContent())
                .member(member)
                .build();
    }

    public static PostResponse.PostsResDTO toPostsResDTO(Post post) {
        return PostResponse.PostsResDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .imageUrl(null)
                .build();
    }

    public static PostResponse.PostPreViewListDTO toPostPreViewListDTO(Page<Post> postPage) {
        List<PostResponse.PostsResDTO> list = postPage.getContent().stream()
                .map(PostConverter::toPostsResDTO)
                .toList();

        return PostResponse.PostPreViewListDTO.builder()
                .postList(list)
                .hasNext(postPage.hasNext())
                .build();
    }

    public static PostResponse.PostDetailResDTO toPostDetailResDTO(Post post, boolean isLiked, boolean isMine) {
        return PostResponse.PostDetailResDTO.builder()
                .postId(post.getId())
                .profileImageUrl(post.getMember().getProfileImage())
                .nickname(post.getMember().getName())
                .createdAt(post.getCreatedAt())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(new ArrayList<>())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(isLiked)
                .isSelfDeveloped(post.isSelfDeveloped())
                .isMine(isMine)
                .commentCount(post.getCommentCount())

                // 현상소 리뷰
                .labReview(post.getPhotoLab() != null ? PostResponse.LabReviewResDTO.builder()
                        .labId(post.getPhotoLab().getId())
                        .labName(post.getPhotoLab().getName())
                        .content(post.getLabReview())
                        .build() : null)
                .build();
    }
}