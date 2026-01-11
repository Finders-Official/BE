package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.PostResponse;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.PostLikeRepository;
import com.finders.api.domain.community.repository.PostQueryRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort; // 💡 Sort 임포트 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostQueryRepository postQueryRepository;

    @Override
    public PostResponse.PostDetailResDTO getPostDetail(Long postId, Member member) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        boolean isLiked = postLikeRepository.existsByPostAndMember(post, member);
        boolean isMine = post.getMember().getId().equals(member.getId());

        return PostResponse.PostDetailResDTO.from(post, isLiked, isMine);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPostList(Integer page) {
        Page<Post> postPage = postRepository.findAll(
                PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<PostResponse.PostPreviewDTO> dtos = postPage.getContent().stream()
                .map(post -> PostResponse.PostPreviewDTO.from(post, false))
                .toList();

        return PostResponse.PostPreviewListDTO.from(dtos);
    }

    @Override
    public PostResponse.PostPreviewListDTO getPopularPosts(Member member) {
        List<Post> posts = postQueryRepository.findTop10PopularPosts();

        List<PostResponse.PostPreviewDTO> previewDTOs = posts.stream()
                .map(post -> {
                    boolean isLiked = (member != null) && postLikeRepository.existsByPostAndMember(post, member);
                    return PostResponse.PostPreviewDTO.from(post, isLiked);
                })
                .toList();

        return PostResponse.PostPreviewListDTO.from(previewDTOs);
    }
}