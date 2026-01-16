package com.finders.api.infra.storage;

import com.finders.api.infra.storage.StorageResponse.SignedUrl;
import java.util.List;
import java.util.Map;

/**
 * Storage 서비스 인터페이스
 * <p>
 * 도메인 서비스에서 주입받아 사용.
 * GCS 외 다른 스토리지(S3, Local 등)로 교체 가능하도록 인터페이스 분리.
 */
public interface StorageService {

    // 단건 업로드 경로 생성 및 Presigned URL 발급
    StorageResponse.PresignedUrl generatePresignedUrl(StoragePath storagePath, Long domainId, String originalFileName);

    // 벌크 업로드 경로 생성 및 Presigned URL 발급
    List<StorageResponse.PresignedUrl> generateBulkPresignedUrls(StoragePath storagePath, Long domainId, List<String> fileNames);

    /**
     * 파일 삭제
     *
     * @param objectPath 삭제할 파일 경로
     * @param isPublic   true: public 버킷, false: private 버킷
     * @return 삭제 결과
     */
    StorageResponse.Delete delete(String objectPath, boolean isPublic);

    /**
     * Signed URL 생성 (Private 버킷 전용)
     *
     * @param objectPath    파일 경로
     * @param expiryMinutes 만료 시간 (분), null이면 기본값 사용
     * @return Signed URL
     */
    StorageResponse.SignedUrl getSignedUrl(String objectPath, Integer expiryMinutes);

    /**
     * Public URL 생성 (Public 버킷 전용)
     *
     * @param objectPath 파일 경로
     * @return 직접 접근 가능한 URL
     */
    String getPublicUrl(String objectPath);

    // 업로드(PUT) 전용 URL 발급
    StorageResponse.PresignedUrl getPresignedUrl(String objectPath, boolean isPublic, Integer expiryMinutes);

    // 업로드(PUT) 전용 URL 발급 (벌크/리스트)
    List<StorageResponse.PresignedUrl> getPresignedUrls(List<String> objectPaths, boolean isPublic, Integer expiryMinutes);

    /**
     * 배치로 한번에 Signed URL 생성
     * @param objectPaths 업로드할 파일 경로 List
     * @param expiryMinutes  만료 시간 (분), null이면 기본값 사용
     * @return
     */
    Map<String, SignedUrl> getSignedUrls(List<String> objectPaths, Integer expiryMinutes);

}
