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

    // Single upload path generation with a subPath (e.g., documentType)
    StorageResponse.PresignedUrl generatePresignedUrl(StoragePath storagePath, Long domainId, String subPath, String originalFileName);

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

    /**
     * Private 버킷에서 Public 버킷으로 파일 복사
     * <p>
     * AI 복원 완료 이미지를 커뮤니티에 공유할 때 사용합니다.
     * GCS 내부 복사(Storage.CopyRequest)를 사용하여 네트워크 비용 없이 빠르게 처리합니다.
     *
     * @param sourceObjectPath Private 버킷의 원본 파일 경로 (예: "restorations/123/restored/abc.png")
     * @param storagePath      Public 버킷의 목적지 경로 타입
     * @param domainId         도메인 ID (memberId 등)
     * @param fileName         파일명 (확장자 포함)
     * @return 복사된 파일의 objectPath (예: "temp/456/xyz.png")
     */
    String copyToPublic(String sourceObjectPath, StoragePath storagePath, Long domainId, String fileName);

    /**
     * MultipartFile을 Public 버킷에 업로드
     *
     * @param file        업로드할 파일
     * @param storagePath 저장 경로 타입
     * @param domainId    도메인 ID
     * @return 업로드 결과
     */
    StorageResponse.Upload uploadPublic(org.springframework.web.multipart.MultipartFile file, StoragePath storagePath, Long domainId);

    /**
     * MultipartFile을 Private 버킷에 업로드
     *
     * @param file        업로드할 파일
     * @param storagePath 저장 경로 타입
     * @param domainId    도메인 ID
     * @param subPath     추가 하위 경로 (documentType 등)
     * @return 업로드 결과
     */
    StorageResponse.Upload uploadPrivate(org.springframework.web.multipart.MultipartFile file, StoragePath storagePath, Long domainId, String subPath);

}
