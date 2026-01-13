package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.CommentResponse;
import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.CommentRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {

    private static final int SIGNED_URL_EXPIRY_MINUTES = 60;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final StorageService storageService;


    @Override
    public CommentResponse.CommentListDTO getCommentsByPost(Long postId, MemberUser memberUser) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByPostAndStatusOrderByCreatedAtDesc(post, CommunityStatus.ACTIVE);

        List<CommentResponse.CommentResDTO> commentResDTO = comments.stream()
                .map(comment -> {
                    String profileUrl = getFullUrl(comment.getMemberUser().getProfileImage());
                    return CommentResponse.CommentResDTO.from(comment, memberUser.getId(), profileUrl);
                })
                .toList();

        return CommentResponse.CommentListDTO.from(commentResDTO);
    }

    private String getFullUrl(String path) {
        if (path == null || path.isBlank()) return null;
        return storageService.getSignedUrl(path, SIGNED_URL_EXPIRY_MINUTES).url();
    }
}