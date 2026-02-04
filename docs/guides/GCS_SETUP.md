# GCS(Google Cloud Storage) 설정 가이드

> 관련 이슈: #14
>
> **마지막 업데이트**: 2026-01-30

---

## 버킷 정보

| 버킷 | 용도 | 접근 방식 |
|------|------|----------|
| `finders-public` | 공개 이미지 (프로필, 현상소, 게시글) | 직접 URL |
| `finders-private` | 비공개 파일 (스캔 사진, 서류, AI 복원) | Signed URL |

---

## 팀원 설정 가이드 (필수!)

### 1. gcloud CLI 설치

#### Mac
```bash
brew install --cask google-cloud-sdk
```

#### Windows (WSL/Ubuntu)
```bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates gnupg curl
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
sudo apt-get update && sudo apt-get install -y google-cloud-cli
```

### 2. 로그인 및 인증 설정

```bash
# 1. Google 계정 로그인
gcloud auth login

# 2. 프로젝트 설정
gcloud config set project project-37afc2aa-d3d3-4a1a-8cd

# 3. ADC 설정 (GCS 접근용) - 가장 중요!
gcloud auth application-default login
```

> 브라우저가 열리면 Google 계정으로 로그인하면 끝!

### 3. 설정 확인

```bash
# GCS 버킷 접근 테스트
gsutil ls gs://finders-public
gsutil ls gs://finders-private
```

---

## 환경별 인증 방식

| 환경 | 인증 방법 | 설정 |
|------|----------|------|
| **로컬 개발** | ADC | 위의 `gcloud auth application-default login` 실행 |
| **GCP 서버** | VM 서비스 계정 | 아래 "GCE VM 설정" 참고 |

---

## GCE VM 설정 (서버 배포 시 필수!)

GCE VM에서 GCS Presigned URL(Signed URL)을 생성하려면 추가 설정이 필요합니다.

### 1. OAuth Scope 설정

VM 생성 시 또는 수정 시 다음 scope가 필요합니다:

| Scope | 설명 |
|-------|------|
| `https://www.googleapis.com/auth/cloud-platform` | 모든 GCP API (권장) |
| `https://www.googleapis.com/auth/iam` | IAM API만 (최소 권한) |

#### GCP Console에서 설정
1. **Compute Engine → VM 인스턴스 → [인스턴스 선택]**
2. **중지** (scope 변경은 VM 중지 필요)
3. **수정 → Access scopes**
4. "Allow full access to all Cloud APIs" 선택 또는
5. "Set access for each API" → **Cloud IAM** → **Enabled**
6. **저장 → 시작**

#### gcloud CLI로 설정
```bash
# VM 중지
gcloud compute instances stop [VM_NAME] --zone=[ZONE]

# Scope 변경
gcloud compute instances set-service-account [VM_NAME] \
  --zone=[ZONE] \
  --scopes=cloud-platform

# VM 시작
gcloud compute instances start [VM_NAME] --zone=[ZONE]
```

### 2. IAM 권한 설정

서비스 계정이 **자기 자신을 impersonate** 할 수 있어야 합니다:

```bash
# 서비스 계정에 TokenCreator 역할 부여 (self-impersonation)
gcloud iam service-accounts add-iam-policy-binding \
  [SERVICE_ACCOUNT_EMAIL] \
  --member="serviceAccount:[SERVICE_ACCOUNT_EMAIL]" \
  --role="roles/iam.serviceAccountTokenCreator"
```

### 체크리스트 (GCE 배포)

- [ ] VM에 `cloud-platform` 또는 `iam` scope 설정
- [ ] 서비스 계정에 `iam.serviceAccountTokenCreator` 역할 부여 (self-impersonation)
- [ ] `GCS_SERVICE_ACCOUNT_EMAIL` 환경변수 설정

---

## 경로 규칙

| 용도 | 버킷 | 경로 패턴 |
|------|------|----------|
| 프로필 이미지 | public | `profiles/{memberId}/{uuid}.{ext}` |
| 현상소 이미지 | public | `photo-labs/{photoLabId}/images/{uuid}.{ext}` |
| 게시글 이미지 | public | `posts/{postId}/{uuid}.{ext}` |
| 스캔 사진 | private | `temp/orders/{orderId}/scans/{uuid}.{ext}` (30일 자동삭제) |
| AI 복원 사진 | private | `restorations/{memberId}/{original|restored}/{uuid}.{ext}` |
| 임시 업로드 | public | `temp/{memberId}/{uuid}.{ext}` (30일 후 자동 삭제) |

---

## URL 형식

```
# Public (직접 접근)
https://storage.googleapis.com/finders-public/profiles/123/abc.jpg

# Private (Signed URL)
https://storage.googleapis.com/finders-private/orders/456/scan.jpg?X-Goog-Signature=...
```

---

## 체크리스트

### 로컬 개발
- [ ] gcloud CLI 설치 완료
- [ ] `gcloud auth login` 실행
- [ ] `gcloud config set project project-37afc2aa-d3d3-4a1a-8cd` 실행
- [ ] `gcloud auth application-default login` 실행
- [ ] `gsutil ls gs://finders-public` 접근 확인

## StorageService 메서드 사용 방식
### 메서드 비교: generate vs get
가장 중요한 차이점은 저장 경로(Object Path)의 결정 주체입니다.
- generate... 메서드 계열: 신규 경로 생성 + URL 발급
  - UUID 생성 O
  - 파일을 처음 업로드할 때
  - 중복 가능성 0%
- get... 메서드 계열: 기존 경로 재사용 + URL 발급
  - UUID 생성 X
  - 이미 DB에 있는 파일을 덮어쓰기할 때
  - 기존 파일이 있을 경우 교체됨

### 상황별 메서드 활용법
1. 프론트엔드에서 신규 파일을 올릴 때
    클라이언트가 파일명만 던져주면, 서버가 "어디에 어떤 이름으로 저장할지"를 정해주는 방식 -> `generate...`
2. 이미 업로드된 파일을 교체/수정할 때
    DB에 이미 저장된 objectPath가 있고, 해당 위치에 파일을 다시 올리고 싶을 때 사용 -> `get...`
3. Private 버킷의 파일을 조회할 때
    보안상 외부 노출이 안 되는 Private 버킷의 파일을 클라이언트에게 일시적으로 보여줄 때 사용 -> `getSignedUrl`
4. 서버에서 데이터를 가공하여 저장할 때
    사용자를 거치지 않고 서버가 직접 파일을 생성하거나 수정해 저장할 때 사용 -> `uploadBytes`, `copyToPublic`

### GCE 서버 배포
- [ ] VM OAuth Scope에 `cloud-platform` 또는 `iam` 포함
- [ ] 서비스 계정에 `iam.serviceAccountTokenCreator` 역할 (self-impersonation)
- [ ] `GCS_SERVICE_ACCOUNT_EMAIL` 환경변수 설정

---

## 트러블슈팅

### Presigned URL 생성 시 500 에러

#### 증상
```json
{
  "success": false,
  "code": "COMMON_500",
  "message": "서버 내부 오류가 발생했습니다."
}
```

#### 원인 1: OAuth Scope 부족
```
Caused by: java.io.IOException: Error code 403 trying to sign provided bytes: 
Request had insufficient authentication scopes.
```

**해결**: VM에 `cloud-platform` 또는 `iam` scope 추가 (위의 "GCE VM 설정" 참고)

#### 원인 2: IAM 권한 부족
```
Caused by: com.google.api.client.googleapis.json.GoogleJsonResponseException: 
403 Forbidden - The caller does not have permission
```

**해결**: 서비스 계정에 `iam.serviceAccountTokenCreator` 역할 부여
```bash
gcloud iam service-accounts add-iam-policy-binding \
  [SERVICE_ACCOUNT_EMAIL] \
  --member="serviceAccount:[SERVICE_ACCOUNT_EMAIL]" \
  --role="roles/iam.serviceAccountTokenCreator"
```

#### 원인 3: Signer 초기화 실패
서버 시작 로그에서 확인:
```
[GcsStorageService.initSigner] Signed URL 생성 불가: serviceAccountEmail 미설정
```

**해결**: `GCS_SERVICE_ACCOUNT_EMAIL` 환경변수 설정

### 현재 VM Scope 확인 방법
```bash
gcloud compute instances describe [VM_NAME] \
  --zone=[ZONE] \
  --format="yaml(serviceAccounts)"
```
