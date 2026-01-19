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
                .map(this::toPostImageResDTO)
                .toList();

        return PostResponse.PostDetailResDTO.from(post, isLiked, isMine, profileImageUrl, images);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPostList(Integer page, Long memberId) {
        List<Post> posts = postQueryRepository.findAllForFeed(page, DEFAULT_PAGE_SIZE);
        return PostResponse.PostPreviewListDTO.from(convertToPreviewDTOs(posts, memberId));
    }

    @Override
    public PostResponse.PostPreviewListDTO getPopularPosts(Long memberId) {
        List<Post> posts = postQueryRepository.findTop10PopularPosts();
        return PostResponse.PostPreviewListDTO.from(convertToPreviewDTOs(posts, memberId));
    }

    @Override
    public PostResponse.PostPreviewListDTO searchPosts(String keyword, String filter, Long memberId, Pageable pageable) {
        List<Post> posts = postQueryRepository.searchPosts(
                keyword,
                filter,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Long totalCount = postQueryRepository.countSearchPosts(keyword, filter);

        List<PostResponse.PostPreviewDTO> previewDTOs = convertToPreviewDTOs(posts, memberId);

        boolean isLast = (long) (pageable.getPageNumber() + 1) * pageable.getPageSize() >= totalCount;

        return PostResponse.PostPreviewListDTO.from(previewDTOs, totalCount, isLast);
    }

    private PostResponse.PostImageResDTO toPostImageResDTO(com.finders.api.domain.community.entity.PostImage image) {
        if (image == null) return null;
        return PostResponse.PostImageResDTO.from(image, getFullUrl(image.getObjectPath()));
    }

    private List<PostResponse.PostPreviewDTO> convertToPreviewDTOs(List<Post> posts, Long memberId) {
        Set<Long> likedPostIds = getLikedPostIds(memberId, posts);

        return posts.stream()
                .map(post -> {
                    boolean isLiked = likedPostIds.contains(post.getId());
                    com.finders.api.domain.community.entity.PostImage firstImage = post.getPostImageList().isEmpty() ? null
                            : post.getPostImageList().get(0);

                    return PostResponse.PostPreviewDTO.from(post, isLiked, toPostImageResDTO(firstImage));
                })
                .toList();
    }

    private Set<Long> getLikedPostIds(Long memberId, List<Post> posts) {
        if (memberId == null || posts.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return postLikeRepository.findLikedPostIdsByMemberAndPostIds(memberId, postIds);
    }

    private String getFullUrl(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            return null;
        }
        return storageService.getPublicUrl(objectPath);
    }
}