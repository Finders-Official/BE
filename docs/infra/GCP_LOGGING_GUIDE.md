# GCP 로그 확인 가이드

팀원들이 서버 로그를 확인하는 방법을 안내합니다.

## 사전 준비

### 권한 확인
로그를 확인하려면 다음 권한이 필요합니다:
- `roles/logging.viewer` - 로그 뷰어
- `roles/monitoring.viewer` - 모니터링 뷰어

> 권한이 없다면 프로젝트 관리자에게 요청하세요.

## GCP 콘솔 접속

### 1. GCP 콘솔 로그인
1. [Google Cloud Console](https://console.cloud.google.com) 접속
2. Google 계정으로 로그인 (권한이 부여된 계정)
3. 상단의 프로젝트 선택 드롭다운에서 **Finders 프로젝트** 선택

### 2. 로그 탐색기 접속
1. 좌측 메뉴에서 **Logging** → **로그 탐색기** 클릭
2. 또는 직접 접속: [로그 탐색기](https://console.cloud.google.com/logs/query)

## 로그 조회 방법

### 기본 필터 사용

#### 애플리케이션 로그 조회 (Docker gcplogs)
```
resource.type="global"
logName="projects/project-37afc2aa-d3d3-4a1a-8cd/logs/gcplogs-docker-driver"
```

#### 에러 로그만 조회
```
resource.type="global"
textPayload=~"ERROR|Exception"
```

#### 특정 시간대 로그 조회
- 우측 상단의 시간 범위 선택기 사용
- 기본값: 최근 1시간

### 자주 쓰는 필터

| 용도 | 필터 |
|------|------|
| 전체 앱 로그 | `resource.type="global" logName=~"gcplogs-docker-driver"` |
| ERROR 이상 | `textPayload=~"ERROR"` |
| 특정 키워드 | `textPayload=~"키워드"` |
| 특정 API 에러 | `textPayload=~"/api/posts"` |

### 필터 저장하기
1. 필터 입력 후 **저장** 버튼 클릭
2. 이름 입력 후 저장
3. 다음에 저장된 필터 불러오기 가능

## 모니터링 대시보드

### 대시보드 접속
1. 좌측 메뉴에서 **Monitoring** → **대시보드** 클릭
2. 또는 직접 접속: [모니터링 대시보드](https://console.cloud.google.com/monitoring/dashboards)

### 확인 가능한 지표
- CPU 사용률
- 메모리 사용량
- 네트워크 트래픽
- 디스크 I/O

## SSH 접속 (대안)

GCP 콘솔에서 직접 서버에 SSH 접속하여 로그를 확인할 수도 있습니다.

### 접속 방법
1. **Compute Engine** → **VM 인스턴스** 이동
2. 해당 인스턴스의 **SSH** 버튼 클릭
3. 브라우저에서 터미널 열림

### 로그 파일 직접 확인
```bash
# 애플리케이션 로그 (실시간)
tail -f /var/log/spring-boot/application.log

# 최근 100줄
tail -n 100 /var/log/spring-boot/application.log

# 에러만 필터링
grep -i error /var/log/spring-boot/application.log
```

## 로컬 개발 환경 설정 (Presigned URL 테스트)

GCS Presigned URL을 로컬에서 테스트하려면 서비스 계정 Impersonation 설정이 필요합니다.

### 1. gcloud CLI 설치

아직 설치하지 않았다면 [Google Cloud SDK](https://cloud.google.com/sdk/docs/install)를 설치하세요.

설치 확인:
```bash
gcloud --version
```

### 2. 로그인 및 프로젝트 설정

```bash
# Google 계정으로 로그인 (브라우저 열림)
gcloud auth login

# 프로젝트 설정
gcloud config set project project-37afc2aa-d3d3-4a1a-8cd
```

### 3. 서비스 계정 Impersonation 설정 ⭐

Presigned URL 생성을 위해 서비스 계정을 Impersonate 합니다:

```bash
gcloud auth application-default login \
  --impersonate-service-account=517500643080-compute@developer.gserviceaccount.com
```

브라우저가 열리면 **권한이 부여된 Google 계정**으로 로그인하세요.

> **참고**: 이 권한이 없다면 프로젝트 관리자에게 `roles/iam.serviceAccountTokenCreator` 권한을 요청하세요.

### 4. 애플리케이션 실행

**터미널에서 실행:**
```bash
./gradlew bootRun
```

**IntelliJ에서 실행하는 경우:**
> ADC 설정 후 **IntelliJ를 완전히 재시작**해야 합니다. (프로젝트 닫기가 아닌 IntelliJ 종료 후 재시작)

### 5. Swagger에서 테스트

http://localhost:8080/swagger-ui.html 접속 → **[TEST] Storage** 섹션

#### Step 1: 파일 업로드 테스트

**POST `/storage/test/upload/private`**

| 파라미터 | 값 |
|---------|-----|
| `file` | 테스트할 이미지 파일 선택 |
| `testId` | `1` (기본값) |

→ Execute 후 응답에서 `objectPath` 복사 (예: `scanned/1/abc123.jpg`)

#### Step 2: Signed URL 생성 테스트

**POST `/storage/test/signed-url`**

| 파라미터 | 값 |
|---------|-----|
| `objectPath` | Step 1에서 받은 경로 |
| `expiryMinutes` | `15` (선택) |

→ Execute 후 응답에서 `signedUrl` 복사 → **브라우저에서 열어보기**

이미지가 보이면 성공! 🎉

#### Step 3: 파일 삭제 (정리)

**DELETE `/storage/test`**

| 파라미터 | 값 |
|---------|-----|
| `objectPath` | Step 1에서 받은 경로 |
| `isPublic` | `false` |

### 주의사항

- Impersonation 인증은 **1시간 후 만료**됩니다. 만료 시 3번 단계를 다시 실행하세요.
- 테스트 시 실제 GCS 버킷(`finders-private`, `finders-public`)에 접근합니다.
- 테스트 파일은 `temp/` 경로에 업로드하면 30일 후 자동 삭제됩니다.
- `[TEST] Storage` API는 **local 프로필에서만** 활성화됩니다.

### 트러블슈팅

#### "401 Unauthorized" 오류
- ADC 설정이 안 되어 있습니다 → 3번 단계 실행
- IntelliJ 사용 시 → IntelliJ 완전히 재시작 (종료 후 다시 시작)
- 기존 ADC가 잘못된 경우:
  ```bash
  gcloud auth application-default revoke  # 기존 설정 삭제
  # 3번 단계 다시 실행
  ```

#### "403 Forbidden" 오류
- Impersonation이 만료되었을 수 있습니다 → 3번 단계 다시 실행
- 서버 재시작 후 다시 시도

#### "Permission denied" 오류
- 프로젝트 관리자에게 `serviceAccountTokenCreator` 권한 요청

---

## 문제 해결

### "권한이 없습니다" 오류
- 올바른 Google 계정으로 로그인했는지 확인
- 프로젝트 관리자에게 권한 요청

### 로그가 보이지 않음
- 시간 범위 설정 확인
- 필터 조건 확인 (너무 좁은 조건)
- 프로젝트가 올바르게 선택되었는지 확인

## 참고 링크

- [GCP Cloud Logging 문서](https://cloud.google.com/logging/docs)
- [로그 쿼리 언어 가이드](https://cloud.google.com/logging/docs/view/logging-query-language)
- [Cloud Monitoring 문서](https://cloud.google.com/monitoring/docs)
