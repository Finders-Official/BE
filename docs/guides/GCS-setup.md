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
