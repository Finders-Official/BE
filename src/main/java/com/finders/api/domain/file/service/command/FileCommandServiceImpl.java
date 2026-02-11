package com.finders.api.domain.file.service.command;

import com.finders.api.domain.file.dto.FileRequest;
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
@Transactional
public class FileCommandServiceImpl implements FileCommandService {

    private final StorageService storageService;

    @Override
    public StorageResponse.PresignedUrl getPresignedUrl(Long memberId, FileRequest.GetPresignedUrl request) {
        StoragePath pathType = request.category();

        // 공통 API에서 허용되지 않는 카테고리(오너/관리자용) 차단
        if (!pathType.isCommon()) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_CATEGORY, "해당 카테고리는 도메인 전용 API를 이용해야 합니다.");
        }

        // 공통 카테고리의 경우, domainId는 반드시 본인의 memberId여야 함
        if (!memberId.equals(request.memberId())) {
            throw new CustomException(ErrorCode.STORAGE_ACCESS_DENIED);
        }

        // 고유한 파일명 및 최종 경로 생성
        return storageService.generatePresignedUrl(
                pathType,
                request.memberId(),
                request.fileName()
        );
    }
}
