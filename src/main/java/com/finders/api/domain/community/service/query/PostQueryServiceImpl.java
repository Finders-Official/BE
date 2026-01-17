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
    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberUserRepository memberUserRepository;

    @Override
    public PostResponse.PostDetailResDTO getPostDetail(Long postId, Long memberId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        MemberUser memberUser = (memberId != null) ? memberUserRepository.findById(memberId).orElse(null) : null;

        boolean isLiked = (memberUser != null) && postLikeRepository.existsByPostAndMemberUser(post, memberUser);
        boolean isMine = (memberId != null) && post.getMemberUser().getId().equals(memberId);

        String profileImageUrl = getFullUrl(post.getMemberUser().getProfileImage());

        List<String> imageUrls = post.getPostImageList().stream()
                .map(img -> getFullUrl(img.getImageUrl()))
                .toList();

        return PostResponse.PostDetailResDTO.from(post, isLiked, isMine, profileImageUrl, imageUrls);
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
                    String mainImageUrl = post.getPostImageList().isEmpty() ? null
                            : getFullUrl(post.getPostImageList().get(0).getImageUrl());
                    return PostResponse.PostPreviewDTO.from(post, isLiked, mainImageUrl);
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
                    String mainImageUrl = post.getPostImageList().isEmpty() ? null
                            : getFullUrl(post.getPostImageList().get(0).getImageUrl());

                    return PostResponse.PostPreviewDTO.from(post, isLiked, mainImageUrl);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(previewDTOs);
    }

    private String getFullUrl(String objectPath) {
        return objectPath;
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
                    String mainImageUrl = post.getPostImageList().isEmpty() ? null
                            : getFullUrl(post.getPostImageList().get(0).getImageUrl());
                    return PostResponse.PostPreviewDTO.from(post, isLiked, mainImageUrl);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(dtos);
    }
}