package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.dto.request.PostRequest;
import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.repository.CommentRepository;
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
public class CommentCommandServiceImpl implements CommentCommandService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long createComment(Long postId, PostRequest.CreateCommentDTO request, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Comment comment = Comment.toEntity(request.content(), post, member);

        post.increaseCommentCount();

        return commentRepository.save(comment).getId();
    }

    @Override
    public void deleteComment(Long commentId, Member member) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Post post = comment.getPost();
        commentRepository.delete(comment);
        post.decreaseCommentCount();
    }
}