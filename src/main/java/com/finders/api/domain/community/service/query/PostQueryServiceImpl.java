package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostQueryRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.store.entity.PhotoLab;
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
    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostQueryRepository postQueryRepository;
    private final StorageService storageService;

    @Override
    public PostResponse.PostDetailResDTO getPostDetail(Long postId, MemberUser memberUser) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        boolean isLiked = (memberUser != null) && postLikeRepository.existsByPostAndMemberUser(post, memberUser);
        boolean isMine = (memberUser != null) && post.getMemberUser().getId().equals(memberUser.getId());

        String profileImageUrl = getFullUrl(post.getMemberUser().getProfileImage());

        List<String> imageUrls = post.getPostImageList().stream()
                .map(img -> getFullUrl(img.getImageUrl()))
                .toList();

        return PostResponse.PostDetailResDTO.from(post, isLiked, isMine, profileImageUrl, imageUrls);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPostList(Integer page) {
        List<Post> posts = postQueryRepository.findAllForFeed(page, DEFAULT_PAGE_SIZE);

        List<PostResponse.PostPreviewDTO> dtos = posts.stream()
                .map(post -> {
                    String mainImageUrl = post.getPostImageList().isEmpty() ? null
                            : getFullUrl(post.getPostImageList().get(0).getImageUrl());
                    return PostResponse.PostPreviewDTO.from(post, false, mainImageUrl);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(dtos);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPopularPosts(MemberUser memberUser) {
        List<Post> posts = postQueryRepository.findTop10PopularPosts();

        Set<Long> likedPostIds = java.util.Collections.emptySet();
        if (memberUser != null) {
            List<Long> postIds = posts.stream().map(Post::getId).toList();
            likedPostIds = postLikeRepository.findLikedPostIdsByMemberAndPostIds(memberUser.getId(), postIds);
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

    private String getFullUrl(String path) {
        if (path == null || path.isBlank()) return null;
        return storageService.getSignedUrl(path, SIGNED_URL_EXPIRY_MINUTES).url();
    }

    // 커뮤니티 게시글 검색
    @Override
    public PostResponse.PostPreviewListDTO searchPosts(String keyword, MemberUser memberUser, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsByKeyword(keyword, pageable);

        Set<Long> likedPostIds = java.util.Collections.emptySet();
        if (memberUser != null) {
            List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
            likedPostIds = postLikeRepository.findLikedPostIdsByMemberAndPostIds(memberUser.getId(), postIds);
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

    // 현상소 검색
    @Override
    public PostResponse.PhotoLabSearchListDTO searchPhotoLabs(String keyword, Double latitude, Double longitude, Pageable pageable) {
        Page<PhotoLab> labs = postRepository.searchByName(keyword, pageable);

        List<PostResponse.PhotoLabSearchDTO> dtos = labs.getContent().stream()
                .map(lab -> {
                    String distanceStr = null;

                    if (latitude != null && longitude != null && lab.getLatitude() != null && lab.getLongitude() != null) {
                        double distance = calculateDistance(
                                latitude,
                                longitude,
                                lab.getLatitude().doubleValue(),
                                lab.getLongitude().doubleValue()
                        );
                        distanceStr = String.format("%.1fkm", distance);
                    }

                    return PostResponse.PhotoLabSearchDTO.from(lab, distanceStr);
                })
                .toList();

        return PostResponse.PhotoLabSearchListDTO.from(dtos);
    }

    // 현상소 검색 직선 거리 계산
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.1515 * 1.609344;
    }
}