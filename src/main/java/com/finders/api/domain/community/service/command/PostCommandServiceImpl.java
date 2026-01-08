package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;

    @Override
    public Long createPost(PostRequest.CreatePostDTO request, Member member) {
        // PhotoLabRepository 연결 예정
        PhotoLab photoLab = null;

        Post post = Post.toEntity(request, member, photoLab);

        return postRepository.save(post).getId();
    }

    @Override
    public void deletePost(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.softDelete();
    }
}