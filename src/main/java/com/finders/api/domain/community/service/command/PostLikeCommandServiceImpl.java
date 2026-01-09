package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.response.PostLikeResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostLike;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.Member;
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

    @Override
    public PostLikeResponse.PostLikeResDTO createPostLike(Long postId, Member member) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (postLikeRepository.existsByPostAndMember(post, member)) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        postLikeRepository.save(PostLike.create(post, member));

        post.increaseLikeCount();

        return PostLikeResponse.PostLikeResDTO.builder()
                .likeCount(post.getLikeCount())
                .isLiked(true)
                .build();
    }

    @Override
    public PostLikeResponse.PostLikeResDTO deletePostLike(Long postId, Member member) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        PostLike postLike = postLikeRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();

        return PostLikeResponse.PostLikeResDTO.builder()
                .likeCount(post.getLikeCount())
                .isLiked(false)
                .build();
    }
}