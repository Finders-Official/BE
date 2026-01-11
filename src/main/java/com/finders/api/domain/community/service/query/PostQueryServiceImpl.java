package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostQueryRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public PostResponse.PostDetailResDTO getPostDetail(Long postId, MemberUser memberUser) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        boolean isLiked = (memberUser != null) && postLikeRepository.existsByPostAndMemberUser(post, memberUser);
        boolean isMine = (memberUser != null) && post.getMemberUser().getId().equals(memberUser.getId());

        return PostResponse.PostDetailResDTO.from(post, isLiked, isMine);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPostList(Integer page) {
        List<Post> posts = postQueryRepository.findAllForFeed(page, DEFAULT_PAGE_SIZE);

        List<PostResponse.PostPreviewDTO> dtos = posts.stream()
                .map(post -> PostResponse.PostPreviewDTO.from(post, false))
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
                    return PostResponse.PostPreviewDTO.from(post, isLiked);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(previewDTOs);
    }
}