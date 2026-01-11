package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
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
    private final PhotoLabRepository photoLabRepository;

    @Override
    public Long createPost(PostRequest.CreatePostDTO request, MemberUser memberUser) {
        PhotoLab photoLab = null;

        if (request.labId() != null) {
            photoLab = photoLabRepository.findById(request.labId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        }

        Post post = Post.toEntity(request, memberUser, photoLab);

        return postRepository.save(post).getId();
    }

    @Override
    public void deletePost(Long postId, MemberUser memberUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (post.getStatus() != CommunityStatus.ACTIVE) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        if (!post.getMemberUser().getId().equals(memberUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.softDelete();
    }
}