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

    /**
     * [신규 업로드용] 단건 업로드 경로 생성 및 Presigned URL 발급
     * * - 차이점: 서버가 직접 UUID를 포함한 '고유한 저장 경로(Object Path)'를 새로 생성합니다.
     * - 용도: 클라이언트가 파일을 처음 업로드할 때 사용하며, 파일명 중복을 방지합니다.
     * - 프로세스: 파일명 수신 -> UUID 결합 및 경로 생성 -> 생성된 경로로 URL 발급
     */
    StorageResponse.PresignedUrl generatePresignedUrl(StoragePath storagePath, Long domainId, String originalFileName);

    // Single upload path generation with a subPath (e.g., documentType)
    StorageResponse.PresignedUrl generatePresignedUrl(StoragePath storagePath, Long domainId, String subPath, String originalFileName);

    /**
     * [신규 업로드용] 벌크 업로드 경로 생성 및 Presigned URL 발급
     * * - 차이점: 서버가 직접 UUID를 포함한 '고유한 저장 경로(Object Path)'를 새로 생성합니다.
     * - 용도: 클라이언트가 파일을 처음 업로드할 때 사용하며, 파일명 중복을 방지합니다.
     * - 프로세스: 파일명 수신 -> UUID 결합 및 경로 생성 -> 생성된 경로로 URL 발급
     */
    List<StorageResponse.PresignedUrl> generateBulkPresignedUrls(StoragePath storagePath, Long domainId, List<String> fileNames);

    /**
     * [기존 경로 재사용/덮어쓰기용] 업로드(PUT)용 Presigned URL 생성 (단건)
     * * - 차이점: 서버가 경로를 생성하지 않고, 파라미터로 받은 '기존 objectPaths'를 그대로 사용합니다.
     * - 용도: 이미 DB에 저장된 파일 경로에 대해 다시 업로드(덮어쓰기) 권한이 필요할 때 사용합니다.
     * - 프로세스: 기존 경로 수신 -> 해당 경로에 대해 즉시 서명(Sign)된 URL 발급
     */
    StorageResponse.PresignedUrl getPresignedUrl(String objectPath, boolean isPublic, Integer expiryMinutes);

    /**
     * [기존 경로 재사용/덮어쓰기용] 업로드(PUT)용 Presigned URL 생성 (벌크/리스트)
     * * - 차이점: 서버가 경로를 생성하지 않고, 파라미터로 받은 '기존 objectPaths'를 그대로 사용합니다.
     * - 용도: 이미 DB에 저장된 파일 경로에 대해 다시 업로드(덮어쓰기) 권한이 필요할 때 사용합니다.
     * - 프로세스: 기존 경로 수신 -> 해당 경로에 대해 즉시 서명(Sign)된 URL 발급
     */
    List<StorageResponse.PresignedUrl> getPresignedUrls(List<String> objectPaths, boolean isPublic, Integer expiryMinutes);

    /**
     * Signed URL 생성 (Private 버킷 전용)
     *
     * @param objectPath    파일 경로
     * @param expiryMinutes 만료 시간 (분), null이면 기본값 사용
     * @return Signed URL
     */
    StorageResponse.SignedUrl getSignedUrl(String objectPath, Integer expiryMinutes);

    /**
     * 배치로 한번에 Signed URL 생성
     * @param objectPaths 업로드할 파일 경로 List
     * @param expiryMinutes  만료 시간 (분), null이면 기본값 사용
     * @return
     */
    Map<String, SignedUrl> getSignedUrls(List<String> objectPaths, Integer expiryMinutes);

    /**
     * Public URL 생성 (Public 버킷 전용)
     *
     * @param objectPath 파일 경로
     * @return 직접 접근 가능한 URL
     */
    String getPublicUrl(String objectPath);

    /**
     * 저장된 값을 클라이언트에 전달 가능한 URL로 변환
     * <p>
     * - 이미 완전한 URL(http/https)이면 그대로 반환
     * - GCS object path이면 Public URL로 변환
     * - null/blank이면 null 반환
     * <p>
     * OAuth 프로필 이미지(외부 URL)와 GCS object path가 혼재하는 컬럼에서 안전하게 사용합니다.
     *
     * @param value 외부 URL 또는 GCS object path
     * @return 접근 가능한 전체 URL, 또는 null
     */
    default String resolveUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return getPublicUrl(value);
    }

    /**
     * 파일 삭제
     *
     * @param objectPath 삭제할 파일 경로
     * @param isPublic   true: public 버킷, false: private 버킷
     * @return 삭제 결과
     */
    StorageResponse.Delete delete(String objectPath, boolean isPublic);

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
}
