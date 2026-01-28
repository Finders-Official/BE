package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.SearchHistoryResponse;
import com.finders.api.domain.community.entity.SearchHistory;
import com.finders.api.domain.community.repository.SearchHistoryRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryQueryServiceImpl implements SearchHistoryQueryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final MemberUserRepository memberUserRepository;
    private final StorageService storageService;

    @Override
    public List<SearchHistoryResponse.SearchHistoryResDTO> getRecentSearchHistories(Long memberId) {
        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<SearchHistory> histories = searchHistoryRepository.findAllByMemberUserOrderByUpdatedAtDesc(memberUser);

        return histories.stream()
                .map(history -> SearchHistoryResponse.SearchHistoryResDTO.from(history, getFullUrl(history.getObjectPath())))
                .collect(Collectors.toList());
    }

    private String getFullUrl(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            return null;
        }
        return storageService.getPublicUrl(objectPath);
    }
}
