package com.finders.api.infra.storage;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.storage.StorageResponse.SignedUrl;
import com.google.auth.ServiceAccountSigner;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
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

    private static final List<String> STORAGE_SCOPES = List.of(
            "https://www.googleapis.com/auth/devstorage.read_only"
    );

    private final Storage storage;
    private final StorageProperties properties;

    private ServiceAccountSigner signer;

    /**
     * IAM Signing을 위한 ServiceAccountSigner 초기화
     * - 서비스 계정 이메일이 설정된 경우: ImpersonatedCredentials 사용 (GCE 환경)
     * - JSON 키 파일 사용 시: GoogleCredentials가 ServiceAccountSigner (로컬 환경)
     */
    @PostConstruct
    void initSigner() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            if (properties.serviceAccountEmail() != null && !properties.serviceAccountEmail().isBlank()) {
                // GCE 환경: ImpersonatedCredentials로 서명
                this.signer = ImpersonatedCredentials.create(
                        credentials,
                        properties.serviceAccountEmail(),
                        null,
                        STORAGE_SCOPES,
                        3600
                );
                log.info("[GcsStorageService] IAM Signing 활성화: {}", properties.serviceAccountEmail());
            } else if (credentials instanceof ServiceAccountSigner) {
                // 로컬 환경: JSON 키 파일 사용 시
                this.signer = (ServiceAccountSigner) credentials;
                log.info("[GcsStorageService] ServiceAccount 키 파일로 서명");
            } else {
                log.warn("[GcsStorageService] Signed URL 생성 불가: serviceAccountEmail 미설정");
            }
        } catch (IOException e) {
            log.error("[GcsStorageService] Signer 초기화 실패: {}", e.getMessage());
        }
    }

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

            storage.createFrom(blobInfo, file.getInputStream());

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
        if (signer == null) {
            throw new CustomException(ErrorCode.STORAGE_SIGNED_URL_FAILED,
                    "Signed URL 생성 불가: Signer가 초기화되지 않았습니다");
        }

        int expiry = expiryMinutes != null ? expiryMinutes : properties.signedUrlExpiryMinutes();

        BlobInfo blobInfo = BlobInfo.newBuilder(properties.privateBucket(), objectPath).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiry,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.signWith(signer)
        );

        long expiresAt = System.currentTimeMillis() / 1000 + (expiry * 60L);

        log.debug("[GcsStorageService.getSignedUrl] path={}, expiryMinutes={}", objectPath, expiry);

        return new StorageResponse.SignedUrl(signedUrl.toString(), expiresAt);
    }

    @Override
    public String getPublicUrl(String objectPath) {
        return String.format("%s/%s", properties.getPublicBaseUrl(), objectPath);
    }


    /**
     * 업로드(PUT)용 Signed URL
     * - private 버킷에 프론트가 직접 PUT 업로드할 때 사용
     */
    @Override
    public StorageResponse.SignedUrl getSignedUploadUrl(String objectPath, Integer expiryMinutes) {
        if (signer == null) {
            throw new CustomException(ErrorCode.STORAGE_SIGNED_URL_FAILED,
                    "Signed URL 생성 불가: Signer가 초기화되지 않았습니다");
        }

        int expiry = expiryMinutes != null ? expiryMinutes : properties.signedUrlExpiryMinutes();

        BlobInfo blobInfo = BlobInfo.newBuilder(properties.privateBucket(), objectPath).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                expiry,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.signWith(signer)
        );

        long expiresAt = System.currentTimeMillis() / 1000 + (expiry * 60L);

        log.debug("[GcsStorageService.getSignedUploadUrl] path={}, expiryMinutes={}", objectPath, expiry);

        return new StorageResponse.SignedUrl(signedUrl.toString(), expiresAt);
    }

    @Override
    public Map<String, SignedUrl> getSignedUrls(List<String> objectPaths, Integer expiryMinutes) {
        int expiry = expiryMinutes != null ? expiryMinutes : properties.signedUrlExpiryMinutes();

        return objectPaths.stream()
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p,
                        p -> getSignedUrl(p, expiry) // 내부에서 signUrl 수행
                ));
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
