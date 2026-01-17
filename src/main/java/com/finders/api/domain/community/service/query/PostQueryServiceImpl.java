package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostQueryRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberUserRepository memberUserRepository;
    private final StorageService storageService;

    @Override
    public PostResponse.PostDetailResDTO getPostDetail(Long postId, Long memberId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        MemberUser memberUser = (memberId != null) ? memberUserRepository.findById(memberId).orElse(null) : null;

        boolean isLiked = (memberUser != null) && postLikeRepository.existsByPostAndMemberUser(post, memberUser);
        boolean isMine = (memberId != null) && post.getMemberUser().getId().equals(memberId);

        String profileImageUrl = getFullUrl(post.getMemberUser().getProfileImage());

        List<PostResponse.PostImageResDTO> images = post.getPostImageList().stream()
                .map(img -> PostResponse.PostImageResDTO.builder()
                        .imageUrl(getFullUrl(img.getObjectPath()))
                        .width(img.getWidth())
                        .height(img.getHeight())
                        .build())
                .toList();

        return PostResponse.PostDetailResDTO.from(post, isLiked, isMine, profileImageUrl, images);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPostList(Integer page, Long memberId) {
        List<Post> posts = postQueryRepository.findAllForFeed(page, DEFAULT_PAGE_SIZE);

        Set<Long> likedPostIds = java.util.Collections.emptySet();
        if (memberId != null && !posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).toList();
            likedPostIds = postLikeRepository.findLikedPostIdsByMemberAndPostIds(memberId, postIds);
        }

        final Set<Long> finalLikedPostIds = likedPostIds;
        List<PostResponse.PostPreviewDTO> dtos = posts.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getId());
                    PostResponse.PostImageResDTO mainImage = post.getPostImageList().isEmpty() ? null
                            : PostResponse.PostImageResDTO.builder()
                            .imageUrl(getFullUrl(post.getPostImageList().get(0).getObjectPath()))
                            .width(post.getPostImageList().get(0).getWidth())
                            .height(post.getPostImageList().get(0).getHeight())
                            .build();
                    return PostResponse.PostPreviewDTO.from(post, isLiked, mainImage);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(dtos);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPopularPosts(Long memberId) {
        List<Post> posts = postQueryRepository.findTop10PopularPosts();

        Set<Long> likedPostIds = java.util.Collections.emptySet();
        if (memberId != null) {
            List<Long> postIds = posts.stream().map(Post::getId).toList();
            likedPostIds = postLikeRepository.findLikedPostIdsByMemberAndPostIds(memberId, postIds);
        }

        final Set<Long> finalLikedPostIds = likedPostIds;
        List<PostResponse.PostPreviewDTO> previewDTOs = posts.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getId());
                    PostResponse.PostImageResDTO mainImage = post.getPostImageList().isEmpty() ? null
                            : PostResponse.PostImageResDTO.builder()
                            .imageUrl(getFullUrl(post.getPostImageList().get(0).getObjectPath()))
                            .width(post.getPostImageList().get(0).getWidth())
                            .height(post.getPostImageList().get(0).getHeight())
                            .build();

                    return PostResponse.PostPreviewDTO.from(post, isLiked, mainImage);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(previewDTOs);
    }

    private String getFullUrl(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            return null;
        }
        return storageService.getPublicUrl(objectPath);
    }

    // 커뮤니티 게시글 검색
    @Override
    public PostResponse.PostPreviewListDTO searchPosts(String keyword, Long memberId, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsByKeyword(keyword, pageable);

        Set<Long> likedPostIds = java.util.Collections.emptySet();
        if (memberId != null) {
            List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
            likedPostIds = postLikeRepository.findLikedPostIdsByMemberAndPostIds(memberId, postIds);
        }

        final Set<Long> finalLikedPostIds = likedPostIds;
        List<PostResponse.PostPreviewDTO> dtos = posts.getContent().stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getId());
                    PostResponse.PostImageResDTO mainImage = post.getPostImageList().isEmpty() ? null
                            : PostResponse.PostImageResDTO.builder()
                            .imageUrl(getFullUrl(post.getPostImageList().get(0).getObjectPath()))
                            .width(post.getPostImageList().get(0).getWidth())
                            .height(post.getPostImageList().get(0).getHeight())
                            .build();
                    return PostResponse.PostPreviewDTO.from(post, isLiked, mainImage);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(dtos);
    }
}