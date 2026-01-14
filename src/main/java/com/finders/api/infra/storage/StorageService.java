package com.finders.api.infra.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage 서비스 인터페이스
 * <p>
 * 도메인 서비스에서 주입받아 사용.
 * GCS 외 다른 스토리지(S3, Local 등)로 교체 가능하도록 인터페이스 분리.
 */
public interface StorageService {

    /**
     * Public 버킷에 파일 업로드
     *
     * @param file        업로드할 파일
     * @param storagePath 저장 경로 패턴 (isPublic=true인 경로만 허용)
     * @param pathArgs    경로 패턴에 대입할 인자들 (파일명 제외, UUID 자동 생성)
     * @return 직접 접근 가능한 URL 포함 응답
     */
    StorageResponse.Upload uploadPublic(MultipartFile file, StoragePath storagePath, Object... pathArgs);

    /**
     * Private 버킷에 파일 업로드
     *
     * @param file        업로드할 파일
     * @param storagePath 저장 경로 패턴 (isPublic=false인 경로만 허용)
     * @param pathArgs    경로 패턴에 대입할 인자들 (파일명 제외, UUID 자동 생성)
     * @return object path만 포함 (URL은 getSignedUrl로 별도 요청)
     */
    StorageResponse.Upload uploadPrivate(MultipartFile file, StoragePath storagePath, Object... pathArgs);

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

    /**
     *  업로드(PUT)용 Signed URL 생성 (Private 버킷 전용)
     *
     * @param objectPath    업로드할 파일 경로
     * @param expiryMinutes 만료 시간 (분), null이면 기본값 사용
     * @return PUT Signed URL
     */
    StorageResponse.SignedUrl getSignedUploadUrl(String objectPath, Integer expiryMinutes);
}
