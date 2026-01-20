package com.finders.api.domain.community.service.query;

import com.finders.api.domain.community.dto.response.SearchHistoryResponse;
import java.util.List;

public interface SearchHistoryQueryService {
    List<SearchHistoryResponse.SearchHistoryResDTO> getRecentSearchHistories(Long memberId);
}
