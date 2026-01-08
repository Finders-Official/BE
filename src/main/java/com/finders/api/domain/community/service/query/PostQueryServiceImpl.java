package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.converter.PostConverter;
import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    public PostResponse.PostDetailResDTO getPostDetail(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        boolean isLiked = postLikeRepository.existsByPostAndMember(post, member);

        boolean isMine = post.getMember().getId().equals(member.getId());

        return PostConverter.toPostDetailResDTO(post, isLiked, isMine);
    }

    @Override
    public PostResponse.PostPreViewListDTO getPostList(Integer page) {
        Page<Post> postPage = postRepository.findAll(PageRequest.of(page, 10));

        return PostConverter.toPostPreViewListDTO(postPage);
    }
}