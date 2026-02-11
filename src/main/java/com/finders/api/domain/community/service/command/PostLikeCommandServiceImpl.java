package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.response.PostLikeResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostLike;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.community.service.PopularPostCacheService;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeCommandServiceImpl implements PostLikeCommandService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MemberUserRepository memberUserRepository;
    private final PopularPostCacheService popularPostCacheService;

    @Override
    public PostLikeResponse.PostLikeResDTO createPostLike(Long postId, Long memberId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (postLikeRepository.existsByPostAndMemberUser(post, memberUser)) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        postLikeRepository.save(PostLike.create(post, memberUser));

        post.increaseLikeCount();
        popularPostCacheService.evictPopularPosts();

        return PostLikeResponse.PostLikeResDTO.builder()
                .postId(post.getId())
                .likeCount(post.getLikeCount())
                .isLiked(true)
                .build();
    }

    @Override
    public PostLikeResponse.PostLikeResDTO deletePostLike(Long postId, Long memberId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        PostLike postLike = postLikeRepository.findByPostAndMemberUser(post, memberUser)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();
        popularPostCacheService.evictPopularPosts();

        return PostLikeResponse.PostLikeResDTO.builder()
                .postId(post.getId())
                .likeCount(post.getLikeCount())
                .isLiked(false)
                .build();
    }
}