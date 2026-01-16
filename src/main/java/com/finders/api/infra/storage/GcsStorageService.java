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
            "https://www.googleapis.com/auth/devstorage.read_write"
    );
    private static final int IMPERSONATED_CREDENTIALS_LIFETIME_SECONDS = 3600;

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
                        IMPERSONATED_CREDENTIALS_LIFETIME_SECONDS
                );
                log.info("[GcsStorageService.initSigner] IAM Signing 활성화: {}", properties.serviceAccountEmail());
            } else if (credentials instanceof ServiceAccountSigner) {
                // 로컬 환경: JSON 키 파일 사용 시
                this.signer = (ServiceAccountSigner) credentials;
                log.info("[GcsStorageService.initSigner] ServiceAccount 키 파일로 서명");
            } else {
                log.warn("[GcsStorageService.initSigner] Signed URL 생성 불가: serviceAccountEmail 미설정");
            }
        } catch (IOException e) {
            log.error("[GcsStorageService.initSigner] Signer 초기화 실패: {}", e.getMessage());
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
        validateSignerInitialized();

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
     * 업로드(PUT)용 Presigned URL 생성 (단일)
     * - isPublic에 따라 버킷을 동적으로 선택합니다.
     * - 새로운 응답 DTO(PresignedUrl)를 사용하여 objectPath를 함께 반환합니다.
     */
    @Override
    public StorageResponse.PresignedUrl getPresignedUrl(String objectPath, boolean isPublic, Integer expiryMinutes) {
        // 1. IAM Signer 초기화 확인 (기존 로직 활용)
        validateSignerInitialized();

        // 2. 만료 시간 설정
        int expiry = expiryMinutes != null ? expiryMinutes : properties.signedUrlExpiryMinutes();

        // 3. [핵심] isPublic 여부에 따라 버킷 결정
        String bucketName = isPublic ? properties.publicBucket() : properties.privateBucket();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath).build();

        // 4. PUT 메서드 전용 V4 서명 URL 생성
        URL signedUrl = storage.signUrl(
                blobInfo,
                expiry,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT), // 반드시 PUT
                Storage.SignUrlOption.signWith(signer)            // IAM Signer 사용
        );

        long expiresAt = (System.currentTimeMillis() / 1000) + (expiry * 60L);

        log.debug("[GcsStorageService.getPresignedUpload] bucket={}, path={}", bucketName, objectPath);

        return StorageResponse.PresignedUrl.of(signedUrl.toString(), objectPath, expiresAt);
    }

    /**
     * 업로드(PUT)용 Presigned URL 생성 (벌크/리스트)
     * - 오너의 스캔본 업로드와 같이 여러 개의 URL이 필요할 때 사용합니다.
     */
    @Override
    public List<StorageResponse.PresignedUrl> getPresignedUrls(List<String> objectPaths, boolean isPublic, Integer expiryMinutes) {
        return objectPaths.stream()
                .map(path -> getPresignedUrl(path, isPublic, expiryMinutes))
                .toList();
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

    private void validateSignerInitialized() {
        if (signer == null) {
            throw new CustomException(ErrorCode.STORAGE_SIGNED_URL_FAILED,
                    "Signed URL 생성 불가: Signer가 초기화되지 않았습니다");
        }
    }

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
