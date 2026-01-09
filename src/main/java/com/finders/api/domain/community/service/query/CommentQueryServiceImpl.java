package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.CommentResponse;
import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.CommentRepository;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public CommentResponse.CommentListDTO getCommentsByPost(Long postId, Member member) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByPostAndStatusOrderByCreatedAtDesc(post, CommunityStatus.ACTIVE);
        return CommentResponse.CommentListDTO.from(comments, member.getId());    }
}