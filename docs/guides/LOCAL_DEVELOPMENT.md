# 로컬 개발 환경 설정 가이드

GCS Presigned URL을 로컬에서 테스트하려면 서비스 계정 Impersonation 설정이 필요합니다.

## 1. gcloud CLI 설치

아직 설치하지 않았다면 [Google Cloud SDK](https://cloud.google.com/sdk/docs/install)를 설치하세요.

설치 확인:
```bash
gcloud --version
```

## 2. 로그인 및 프로젝트 설정

```bash
# Google 계정으로 로그인 (브라우저 열림)
gcloud auth login

# 프로젝트 설정
gcloud config set project project-37afc2aa-d3d3-4a1a-8cd
```

## 3. 서비스 계정 Impersonation 설정 ⭐

Presigned URL 생성을 위해 서비스 계정을 Impersonate 합니다:

```bash
gcloud auth application-default login \
  --impersonate-service-account=517500643080-compute@developer.gserviceaccount.com
```

브라우저가 열리면 **권한이 부여된 Google 계정**으로 로그인하세요.

> **참고**: 이 권한이 없다면 프로젝트 관리자에게 `roles/iam.serviceAccountTokenCreator` 권한을 요청하세요.

## 4. 애플리케이션 실행

**터미널에서 실행:**
```bash
./gradlew bootRun
```

**IntelliJ에서 실행하는 경우:**
> ADC 설정 후 **IntelliJ를 완전히 재시작**해야 합니다. (프로젝트 닫기가 아닌 IntelliJ 종료 후 재시작)

## 5. Swagger에서 테스트

http://localhost:8080/swagger-ui.html 접속 → **[TEST] Storage** 섹션

### Step 1: 파일 업로드 테스트

**POST `/storage/test/upload/private`**

| 파라미터 | 값 |
|---------|-----|
| `file` | 테스트할 이미지 파일 선택 |
| `testId` | `1` (기본값) |

→ Execute 후 응답에서 `objectPath` 복사 (예: `scanned/1/abc123.jpg`)

### Step 2: Signed URL 생성 테스트

**POST `/storage/test/signed-url`**

| 파라미터 | 값 |
|---------|-----|
| `objectPath` | Step 1에서 받은 경로 |
| `expiryMinutes` | `15` (선택) |

→ Execute 후 응답에서 `signedUrl` 복사 → **브라우저에서 열어보기**

이미지가 보이면 성공! 🎉

### Step 3: 파일 삭제 (정리)

**DELETE `/storage/test`**

| 파라미터 | 값 |
|---------|-----|
| `objectPath` | Step 1에서 받은 경로 |
| `isPublic` | `false` |

## 주의사항

- Impersonation 인증은 **1시간 후 만료**됩니다. 만료 시 3번 단계를 다시 실행하세요.
- 테스트 시 실제 GCS 버킷(`finders-private`, `finders-public`)에 접근합니다.
- 테스트 파일은 `temp/` 경로에 업로드하면 30일 후 자동 삭제됩니다.
- `[TEST] Storage` API는 **local 프로필에서만** 활성화됩니다.

## 트러블슈팅

### "401 Unauthorized" 오류
- ADC 설정이 안 되어 있습니다 → 3번 단계 실행
- IntelliJ 사용 시 → IntelliJ 완전히 재시작 (종료 후 다시 시작)
- 기존 ADC가 잘못된 경우:
  ```bash
  gcloud auth application-default revoke  # 기존 설정 삭제
  # 3번 단계 다시 실행
  ```

### "403 Forbidden" 오류
- Impersonation이 만료되었을 수 있습니다 → 3번 단계 다시 실행
- 서버 재시작 후 다시 시도

### "Permission denied" 오류
- 프로젝트 관리자에게 `serviceAccountTokenCreator` 권한 요청

## 관련 문서

- [GCS 설정 가이드](./GCS-setup.md)
- [GCP 로그 확인 가이드](../infra/GCP_LOGGING_GUIDE.md)
