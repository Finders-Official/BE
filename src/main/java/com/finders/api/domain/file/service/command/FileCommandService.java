package com.finders.api.domain.file.service.command;

import com.finders.api.domain.file.dto.FileRequest;
import com.finders.api.infra.storage.StorageResponse;

public interface FileCommandService {

    // presigned URL 발급
    StorageResponse.PresignedUrl getPresignedUrl(Long memberId, FileRequest.GetPresignedUrl request);
}
