package com.finders.api.infra.storage;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * GCS Storage 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GcsStorageService implements StorageService {

    private final Storage storage;
    private final StorageProperties properties;

    @Override
    public StorageResponse.Upload uploadPublic(MultipartFile file, StoragePath storagePath, Object... pathArgs) {
        validatePublicPath(storagePath);
        return upload(file, properties.publicBucket(), storagePath, true, pathArgs);
    }

    @Override
    public StorageResponse.Upload uploadPrivate(MultipartFile file, StoragePath storagePath, Object... pathArgs) {
        validatePrivatePath(storagePath);
        return upload(file, properties.privateBucket(), storagePath, false, pathArgs);
    }

    private StorageResponse.Upload upload(MultipartFile file, String bucket,
                                          StoragePath storagePath, boolean isPublic, Object... pathArgs) {
        try {
            String filename = generateFilename(file.getOriginalFilename());
            Object[] allArgs = appendFilename(pathArgs, filename);
            String objectPath = storagePath.format(allArgs);

            BlobId blobId = BlobId.of(bucket, objectPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getInputStream());

            String url = isPublic ? getPublicUrl(objectPath) : null;

            log.info("[GcsStorageService.upload] bucket={}, path={}, size={}",
                    bucket, objectPath, file.getSize());

            return StorageResponse.Upload.from(
                    bucket,
                    objectPath,
                    url,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException e) {
            log.error("[GcsStorageService.upload] 업로드 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STORAGE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public StorageResponse.Delete delete(String objectPath, boolean isPublic) {
        String bucket = isPublic ? properties.publicBucket() : properties.privateBucket();

        BlobId blobId = BlobId.of(bucket, objectPath);
        boolean deleted = storage.delete(blobId);

        log.info("[GcsStorageService.delete] bucket={}, path={}, deleted={}",
                bucket, objectPath, deleted);

        return new StorageResponse.Delete(bucket, objectPath, deleted);
    }

    @Override
    public StorageResponse.SignedUrl getSignedUrl(String objectPath, Integer expiryMinutes) {
        int expiry = expiryMinutes != null ? expiryMinutes : properties.signedUrlExpiryMinutes();

        BlobInfo blobInfo = BlobInfo.newBuilder(properties.privateBucket(), objectPath).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiry,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        );

        long expiresAt = System.currentTimeMillis() / 1000 + (expiry * 60L);

        log.debug("[GcsStorageService.getSignedUrl] path={}, expiryMinutes={}", objectPath, expiry);

        return new StorageResponse.SignedUrl(signedUrl.toString(), expiresAt);
    }

    @Override
    public String getPublicUrl(String objectPath) {
        return String.format("%s/%s", properties.getPublicBaseUrl(), objectPath);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private String generateFilename(String originalFilename) {
        String extension = extractExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private Object[] appendFilename(Object[] pathArgs, String filename) {
        Object[] result = Arrays.copyOf(pathArgs, pathArgs.length + 1);
        result[pathArgs.length] = filename;
        return result;
    }

    private void validatePublicPath(StoragePath storagePath) {
        if (!storagePath.isPublic()) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_PATH,
                    "Public 버킷에 Private 경로를 사용할 수 없습니다: " + storagePath);
        }
    }

    private void validatePrivatePath(StoragePath storagePath) {
        if (storagePath.isPublic()) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_PATH,
                    "Private 버킷에 Public 경로를 사용할 수 없습니다: " + storagePath);
        }
    }
}
