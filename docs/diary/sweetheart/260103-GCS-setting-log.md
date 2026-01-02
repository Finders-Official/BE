# GCS(Google Cloud Storage) 설정 가이드

> 작성자: 스위트하트 김덕환
> 작성일: 2026-01-03
> 관련 이슈: #14

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
| **GCP 서버** | 자동 | 설정 필요 없음 |

---

## 경로 규칙

| 용도 | 버킷 | 경로 패턴 |
|------|------|----------|
| 프로필 이미지 | public | `profiles/{memberId}/{uuid}.{ext}` |
| 현상소 이미지 | public | `labs/{photoLabId}/images/{uuid}.{ext}` |
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

- [ ] gcloud CLI 설치 완료
- [ ] `gcloud auth login` 실행
- [ ] `gcloud config set project project-37afc2aa-d3d3-4a1a-8cd` 실행
- [ ] `gcloud auth application-default login` 실행
- [ ] `gsutil ls gs://finders-public` 접근 확인
