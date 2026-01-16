package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostImage;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.PostImageRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;
    private final PhotoLabRepository photoLabRepository;
    private final PostImageRepository postImageRepository;
    private final StorageService storageService;
    private final MemberUserRepository memberUserRepository;

    @Override
    public Long createPost(PostRequest.CreatePostDTO request, Long memberId) {
        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 자가 현상이 아닐 때만 글자 수 체크
        if (!request.isSelfDeveloped()) {
            String review = request.reviewContent();

            if (review == null || review.trim().isEmpty() || review.length() < 20) {
                throw new CustomException(ErrorCode.REVIEW_TOO_SHORT);
            }
            if (review.length() > 300) {
                throw new CustomException(ErrorCode.REVIEW_TOO_LONG);
            }
        }

        PhotoLab photoLab = null;

        if (request.labId() != null) {
            photoLab = photoLabRepository.findById(request.labId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        }

        Post post = Post.toEntity(request, memberUser, photoLab);
        post = postRepository.save(post);

        if (request.images() != null && !request.images().isEmpty()) {
            for (int i = 0; i < request.images().size(); i++) {
                var uploadResponse = storageService.uploadPublic(
                        request.images().get(i),
                        StoragePath.POST_IMAGE,
                        post.getId()
                );

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .imageUrl(uploadResponse.objectPath())
                        .displayOrder(i)
                        .build();

                postImageRepository.save(postImage);
            }
        }

        return post.getId();
    }

    @Override
    public void deletePost(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (post.getStatus() != CommunityStatus.ACTIVE) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        if (!post.getMemberUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.softDelete();
    }
}