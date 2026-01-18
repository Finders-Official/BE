package com.finders.api.domain.file.service.query;

import com.finders.api.infra.storage.StorageResponse;

public interface FileQueryService {

    // 조회용 URL 발급
    StorageResponse.SignedUrl getSignedUrl(Long memberId, String objectPath);
}
