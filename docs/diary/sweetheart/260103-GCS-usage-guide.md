# StorageService 사용 가이드

> 작성자: 스위트하트 김덕환
> 작성일: 2026-01-03
> 관련 이슈: #14
> 선행 문서: [GCS 설정 가이드](./260103-GCS-setting-log.md)

---

## 1. StorageService 주입

```java
@Service
@RequiredArgsConstructor
public class MemberService {

    private final StorageService storageService;  // 주입

}
```

---

## 2. 파일 업로드

### Public 버킷 (프로필, 현상소, 게시글 이미지)

```java
public void updateProfileImage(Long memberId, MultipartFile file) {
    // 업로드
    StorageResponse.Upload result = storageService.uploadPublic(
        file,                    // 파일
        StoragePath.PROFILE,     // 경로 패턴
        memberId                 // 경로에 들어갈 ID
    );

    // 결과
    result.url();         // "https://storage.googleapis.com/finders-public/profiles/123/uuid.jpg"
    result.objectPath();  // "profiles/123/uuid.jpg" (DB 저장용)
    result.size();        // 파일 크기 (bytes)
    result.contentType(); // "image/jpeg"
}
```

### Private 버킷 (스캔 사진, 복원 사진)

```java
public void uploadScannedPhoto(Long orderId, MultipartFile file) {
    StorageResponse.Upload result = storageService.uploadPrivate(
        file,
        StoragePath.SCANNED_PHOTO,
        orderId
    );

    // Private은 URL이 null! objectPath만 저장
    result.objectPath();  // "temp/orders/456/scans/uuid.jpg"
}
```

---

## 3. 파일 접근

### Public 파일 → URL 직접 사용

```java
// DB에 저장된 objectPath로 URL 생성
String url = storageService.getPublicUrl("profiles/123/uuid.jpg");
// → "https://storage.googleapis.com/finders-public/profiles/123/uuid.jpg"
```

### Private 파일 → Signed URL 생성

```java
// 60분 유효한 Signed URL 생성 (기본값)
StorageResponse.SignedUrl result = storageService.getSignedUrl(
    "temp/orders/456/scans/uuid.jpg",
    null  // null이면 기본 60분
);

// 15분 유효한 Signed URL
StorageResponse.SignedUrl result = storageService.getSignedUrl(
    "temp/orders/456/scans/uuid.jpg",
    15
);

result.url();                  // Signed URL (임시 접근 가능)
result.expiresAtEpochSecond(); // 만료 시간 (Unix timestamp)
```

---

## 4. 파일 삭제

```java
// Public 파일 삭제
StorageResponse.Delete result = storageService.delete(
    "profiles/123/uuid.jpg",
    true  // isPublic = true
);

// Private 파일 삭제
StorageResponse.Delete result = storageService.delete(
    "temp/orders/456/scans/uuid.jpg",
    false  // isPublic = false
);

result.deleted();  // true: 삭제됨, false: 파일 없었음
```

---

## 5. StoragePath 목록

| Enum | 버킷 | 경로 패턴 | 사용처 |
|------|------|----------|--------|
| `PROFILE` | public | `profiles/{memberId}/{uuid}` | 프로필 이미지 |
| `LAB_IMAGE` | public | `labs/{labId}/images/{uuid}` | 현상소 이미지 |
| `LAB_QR` | public | `labs/{labId}/qr.png` | 현상소 QR코드 |
| `POST_IMAGE` | public | `posts/{postId}/{uuid}` | 게시글 이미지 |
| `PROMOTION` | public | `promotions/{id}/{uuid}` | 프로모션 이미지 |
| `TEMP_PUBLIC` | public | `temp/{memberId}/{uuid}` | 임시 업로드 |
| `LAB_DOCUMENT` | private | `labs/{labId}/documents/{type}/{uuid}` | 현상소 서류 |
| `SCANNED_PHOTO` | private | `temp/orders/{orderId}/scans/{uuid}` | 스캔 사진 |
| `RESTORATION_ORIGINAL` | private | `restorations/{memberId}/original/{uuid}` | AI 복원 원본 |
| `RESTORATION_RESTORED` | private | `restorations/{memberId}/restored/{uuid}` | AI 복원 결과 |

---

## 6. 전체 예시 (프로필 이미지 CRUD)

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final StorageService storageService;

    @Transactional
    public String updateProfileImage(Long memberId, MultipartFile file) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 기존 이미지 삭제
        if (member.getProfileImagePath() != null) {
            storageService.delete(member.getProfileImagePath(), true);
        }

        // 새 이미지 업로드
        StorageResponse.Upload result = storageService.uploadPublic(
            file, StoragePath.PROFILE, memberId
        );

        // DB 저장 (objectPath만 저장, URL은 필요할 때 생성)
        member.updateProfileImage(result.objectPath());

        return result.url();
    }

    public String getProfileImageUrl(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getProfileImagePath() == null) {
            return null;
        }

        return storageService.getPublicUrl(member.getProfileImagePath());
    }
}
```

---

## 7. 주의사항

### 로컬 테스트 제한

| 기능 | 로컬 (ADC) | GCP 서버 |
|------|-----------|----------|
| 업로드 | ✅ | ✅ |
| 삭제 | ✅ | ✅ |
| Public URL | ✅ | ✅ |
| **Signed URL** | ❌ | ✅ |

> Signed URL은 서명용 Private Key가 필요해서 로컬에서 테스트 불가.
> 배포 후 테스트 필요!

### DB 저장 팁

```java
// ✅ objectPath 저장 (권장)
member.setProfileImagePath("profiles/123/uuid.jpg");

// ❌ 전체 URL 저장 (비권장 - 버킷명 바뀌면 전부 수정해야 함)
member.setProfileImageUrl("https://storage.googleapis.com/finders-public/...");
```

---

## 8. 테스트 API (local 전용)

Swagger에서 직접 테스트 가능 (local 프로필에서만 활성화):

```
POST /api/storage/test/upload/public   - Public 업로드
POST /api/storage/test/upload/private  - Private 업로드
POST /api/storage/test/signed-url      - Signed URL 생성 (로컬 불가)
DELETE /api/storage/test               - 파일 삭제
```
