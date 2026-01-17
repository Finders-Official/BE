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

    /**
     * byte[] 데이터를 직접 업로드 (서버에서 외부 API 결과를 저장할 때 사용)
     * <p>
     * Replicate AI 복원 결과처럼 백엔드에서 직접 업로드해야 하는 경우에만 사용합니다.
     * 일반적인 클라이언트 업로드는 Presigned URL 방식을 사용하세요.
     *
     * @param data        업로드할 바이트 데이터
     * @param contentType MIME 타입 (예: "image/png")
     * @param storagePath 저장 경로 enum
     * @param domainId    도메인 ID (memberId, orderId 등)
     * @param fileName    파일명 (확장자 포함)
     * @return 업로드 결과 (objectPath 포함)
     */
    StorageResponse.Upload uploadBytes(byte[] data, String contentType, StoragePath storagePath, Long domainId, String fileName);

}
