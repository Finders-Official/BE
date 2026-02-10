package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.CommentResponse;
import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.CommentRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {

    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final StorageService storageService;


    @Override
    public Page<CommentResponse.CommentResDTO> getCommentsByPost(Long postId, Long memberId, Integer page, Integer size) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Comment> commentPage = commentRepository.findAllByPostAndStatusOrderByCreatedAtDesc(post, CommunityStatus.ACTIVE, pageRequest);

        return commentPage.map(comment -> {
            String profileUrl = resolveImageUrl(comment.getMemberUser().getProfileImage());
            return CommentResponse.CommentResDTO.from(comment, memberId, profileUrl);
        });
    }

    private String resolveImageUrl(String value) {
        if (value == null || value.isBlank()) return null;

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        return storageService.getPublicUrl(value);
    }

}