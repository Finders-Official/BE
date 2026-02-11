package com.finders.api.domain.file.service.query;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StoragePath;
import com.finders.api.infra.storage.StorageResponse;
import com.finders.api.infra.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileQueryServiceImpl implements FileQueryService {

    private static final int SIGNED_URL_EXPIRY_MINUTES = 15;

    private final StorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public StorageResponse.SignedUrl getSignedUrl(Long memberId, String objectPath) {
        StoragePath pathType = StoragePath.fromObjectPath(objectPath);

        // 공통 카테고리인지 확인
        if (!pathType.isCommon()) {
            throw new CustomException(ErrorCode.STORAGE_ACCESS_DENIED);
        }

        // 2. 공통 파일이므로 추출된 ID는 반드시 memberId여야 함
        Long pathId = pathType.extractId(objectPath);
        if (!memberId.equals(pathId)) {
            throw new CustomException(ErrorCode.STORAGE_ACCESS_DENIED);
        }

        return storageService.getSignedUrl(objectPath, SIGNED_URL_EXPIRY_MINUTES);
    }
}
