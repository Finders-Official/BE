package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.community.repository.CommentRepository;
import com.finders.api.domain.community.repository.PostRepository;
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
public class CommentCommandServiceImpl implements CommentCommandService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberUserRepository memberUserRepository;

    @Override
    public Long createComment(Long postId, PostRequest.CreateCommentDTO request, Long memberId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Comment comment = Comment.toEntity(request.content(), post, memberUser);

        post.increaseCommentCount();

        return commentRepository.save(comment).getId();
    }

    @Override
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (comment.getStatus() != CommunityStatus.ACTIVE) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        if (!comment.getMemberUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        comment.softDelete();
        Post post = comment.getPost();
        post.decreaseCommentCount();
    }
}