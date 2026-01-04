# GCS 설정 삽질 일기

> 작성일: 2026-01-03
> 다음 프로젝트에서 GCS 설정할 때 참고하자!

---

## 오늘 한 일 요약

1. GCS 버킷 2개 생성 (public/private 분리)
2. Public 버킷 공개 설정
3. CORS 설정
4. Lifecycle 정책 설정
5. 서비스 계정 키 생성 시도 → **실패** (정책 변경됨)
6. ADC 방식으로 전환 → **성공**

---

## 1. 버킷 생성

### 설정 값

```
Cloud Storage → 버킷 → 만들기

- 이름: finders-public (또는 finders-private)
- 위치 유형: Region
- 위치: asia-northeast3 (서울)
- 스토리지 클래스: Standard
- 액세스 제어: 균일 (Uniform) ← 이거 중요!
```

### 왜 버킷을 2개로 나눴나?

- **public**: 프로필, 게시글 이미지 등 누구나 볼 수 있는 것
- **private**: 스캔 사진, 서류 등 Signed URL로만 접근해야 하는 것

처음엔 1개 버킷으로 폴더로 구분하려 했는데, 권한 관리가 복잡해져서 2개로 분리함.

---

## 2. Public 버킷 공개 설정

### GCP Console에서
```
버킷 → 권한 탭 → 액세스 권한 부여
- 주 구성원: allUsers
- 역할: Storage 개체 뷰어
```

### Cloud Shell에서 (더 빠름)
```bash
gsutil iam ch allUsers:objectViewer gs://finders-public
```

---

## 3. CORS 설정

### 중요: GCP Console에서 CORS 설정 UI가 없음!

Cloud Shell에서 명령어로 해야 함.

```bash
# cors.json 파일 생성
cat > cors.json << 'EOF'
[
  {
    "origin": ["*"],
    "method": ["GET", "POST", "PUT", "DELETE", "HEAD"],
    "responseHeader": ["Content-Type", "Authorization", "Content-Length", "Accept"],
    "maxAgeSeconds": 3600
  }
]
EOF

# 적용
gsutil cors set cors.json gs://버킷이름

# 확인
gsutil cors get gs://버킷이름
```

### 주의사항

- `PUT`은 GCS가 파일 업로드에 사용하므로 빼면 안 됨
- 프로덕션에서는 `origin`을 특정 도메인으로 변경할 것

---

## 4. Lifecycle 정책 (임시 파일 자동 삭제)

### GCP Console에서
```
버킷 → 수명 주기 탭 → 규칙 추가
- 작업: 객체 삭제
- 조건:
  - 객체 이름이 프리픽스와 일치함: temp/
  - 수명: 30일
```

### 용도

업로드 후 엔티티에 연결 안 된 임시 파일 자동 정리

---

## 5. 서비스 계정 키 생성 시도 (실패)

### 문제

```
"서비스 계정 키 생성 사용 중지됨"
"조직에 서비스 계정 키 생성을 차단하는 조직 정책이 적용되었습니다"
```

### 원인

**2024년 5월 3일 이후 생성된 GCP 프로젝트는 서비스 계정 키 생성이 기본적으로 막혀있음!**

Google이 보안 강화 차원에서 정책을 변경함.

### 시도한 것들 (다 실패)

1. 조직 정책 페이지에서 수정 시도 → 권한 없음
2. IAM에서 "조직 관리자" 역할 추가 → 안 됨
3. IAM에서 "조직 정책 관리자" 역할 추가 시도 → 역할이 안 보임
4. Cloud Shell에서 명령어로 정책 해제 시도:
   ```bash
   gcloud org-policies reset iam.disableServiceAccountKeyCreation --project=프로젝트ID
   ```
   → 권한 없음

### 결론

개인 GCP 계정에서는 조직 정책을 건드리기 어려움.
**ADC(Application Default Credentials) 방식으로 전환!**

---

## 6. ADC 방식 (성공)

### ADC란?

- Application Default Credentials
- 서비스 계정 키(JSON 파일) 대신 gcloud 로그인 정보를 사용
- Google이 권장하는 최신 방식

### 장점

| 항목 | JSON 키 | ADC |
|------|---------|-----|
| 보안 | 키 유출 위험 | 더 안전 |
| 관리 | 키 파일 공유 필요 | 각자 로그인 |
| GCP 서버 | 키 파일 배포 필요 | 자동 인증 |

### 설정 방법

```bash
# gcloud 설치 후
gcloud auth login
gcloud config set project 프로젝트ID
gcloud auth application-default login  # 이게 핵심!
```

### 환경별 동작

- **로컬**: `~/.config/gcloud/application_default_credentials.json` 자동 생성됨
- **GCP 서버 (Compute Engine)**: 메타데이터 서버에서 자동 인증 (설정 필요 없음!)

---

## 다음 프로젝트에서 체크리스트

### GCS 설정 순서

- [ ] 버킷 생성 (public/private 필요에 따라)
- [ ] 위치: 서비스 대상 지역과 가까운 곳 (한국이면 asia-northeast3)
- [ ] 액세스 제어: 균일 (Uniform)
- [ ] public 버킷이면 allUsers:objectViewer 권한 부여
- [ ] CORS 설정 (Cloud Shell에서 gsutil)
- [ ] Lifecycle 정책 (temp 폴더 등)
- [ ] **JSON 키 대신 ADC 사용!**

### 팀원 온보딩

각 팀원이 해야 할 것:
```bash
gcloud auth login
gcloud config set project 프로젝트ID
gcloud auth application-default login
```

---

## 유용한 명령어 모음

```bash
# 버킷 목록
gsutil ls

# 버킷 내용
gsutil ls gs://버킷이름

# 파일 업로드
gsutil cp 파일 gs://버킷이름/경로/

# 파일 다운로드
gsutil cp gs://버킷이름/경로/파일 ./

# CORS 확인
gsutil cors get gs://버킷이름

# 버킷 권한 확인
gsutil iam get gs://버킷이름

# ADC 토큰 확인
gcloud auth application-default print-access-token
```

---

## 참고 링크

- [GCS 공식 문서](https://cloud.google.com/storage/docs)
- [ADC 인증 가이드](https://cloud.google.com/docs/authentication/provide-credentials-adc)
- [CORS 설정](https://cloud.google.com/storage/docs/cross-origin)
- [서비스 계정 키 생성 차단 문제 해결](https://www.cubebackup.com/docs/tutorials/gcp-allow-service-account-key-creation/)
