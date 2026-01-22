package com.finders.api.domain.community.service.command;

import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostImage;
import com.finders.api.domain.community.entity.SearchHistory;
import com.finders.api.domain.community.repository.PostRepository;
import com.finders.api.domain.community.repository.SearchHistoryRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchHistoryCommandServiceImpl implements SearchHistoryCommandService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final PostRepository postRepository;
    private final MemberUserRepository memberUserRepository;

    private static final int MAX_HISTORY_COUNT = 10;

    @Override
    public void saveSearchHistory(Long memberId, String keyword) {
        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post topPost = postRepository.findTopByKeywordOrderByLikes(keyword).orElse(null);

        String objectPath = null;
        Integer width = null;
        Integer height = null;

        if (topPost != null && !topPost.getPostImageList().isEmpty()) {
            PostImage representativePostImage = topPost.getPostImageList().get(0);
            objectPath = representativePostImage.getObjectPath();
            width = representativePostImage.getWidth();
            height = representativePostImage.getHeight();
        }

        final String finalObjectPath = objectPath;
        final Integer finalWidth = width;
        final Integer finalHeight = height;

        searchHistoryRepository.findByMemberUserAndKeyword(memberUser, keyword)
                .ifPresentOrElse(
                        history -> history.updateSearchInfo(finalObjectPath, finalWidth, finalHeight),
                        () -> {
                            List<SearchHistory> historyList = searchHistoryRepository.findAllByMemberUserOrderByUpdatedAtDesc(memberUser);
                            if (historyList.size() >= MAX_HISTORY_COUNT) {
                                searchHistoryRepository.delete(historyList.get(historyList.size() - 1));
                            }

                            searchHistoryRepository.save(SearchHistory.builder()
                                    .memberUser(memberUser)
                                    .keyword(keyword)
                                    .objectPath(finalObjectPath)
                                    .width(finalWidth)
                                    .height(finalHeight)
                                    .build());
                        }
                );
    }

    @Override
    public void deleteSearchHistory(Long historyId, Long memberId) {
        SearchHistory history = searchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!history.getMemberUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        searchHistoryRepository.delete(history);
    }

    @Override
    public void deleteAllSearchHistory(Long memberId) {
        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        searchHistoryRepository.deleteAllByMemberUser(memberUser);
    }
}
