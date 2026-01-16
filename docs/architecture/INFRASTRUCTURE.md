# Infrastructure

## GCP 프로젝트 정보

| 항목 | 값 |
|------|-----|
| 프로젝트 | My First Project |
| 프로젝트 ID | project-37afc2aa-d3d3-4a1a-8cd |
| 리전 | asia-northeast3 (서울) |

---

## Cloud SQL (MySQL)

| 항목 | 값 |
|------|-----|
| 인스턴스 ID | `finders-db` |
| 버전 | MySQL 8.0 |
| Cloud SQL 버전 | Enterprise |
| 머신 유형 | db-g1-small |
| vCPU | 공유 vCPU |
| RAM | 1.7 GB |
| 저장용량 | 10 GB SSD |
| 가용성 | 단일 영역 |
| 공개 IP | `34.64.50.136` |
| 포트 | 3306 |
| 백업 | 자동 |
| 승인된 네트워크 | `34.50.19.146/32` (finders-server) |
| **예상 비용** | **~$27/월** |

### 접속 정보
```
Host: 34.64.50.136
Port: 3306
Database: finders
User: finders
```

---

## Cloud Storage

### 버킷 구성

| 버킷 | 용도 | 접근 방식 |
|------|------|----------|
| `finders-public` | 공개 이미지 (프로필, 현상소, 게시글) | 직접 URL |
| `finders-private` | 비공개 파일 (스캔 사진, 서류, AI 복원) | Signed URL |

### 공통 설정

| 항목 | 값 |
|------|-----|
| 리전 | asia-northeast3 (서울) |
| 스토리지 클래스 | Standard |
| 액세스 제어 | 균일 (Uniform) |
| CORS | 전체 허용 (`*`) |
| **예상 비용** | **~$1/월** (5GB 무료) |

### 버킷별 설정

| 버킷 | 공개 여부 | Lifecycle |
|------|----------|-----------|
| `finders-public` | `allUsers:objectViewer` | `temp/` 30일 후 삭제 |
| `finders-private` | 비공개 | `temp/` 30일 후 삭제 |

### 서비스 계정

| 항목 | 값 |
|------|-----|
| 이름 | Compute Engine default service account |
| 이메일 | `517500643080-compute@developer.gserviceaccount.com` |
| 역할 | Storage Object Admin |
| 용도 | Spring 서버에서 GCS 접근용 |

### 경로 규칙

| 테이블 | 버킷 | 경로 패턴 |
|--------|------|----------|
| `member` | public | `profiles/{memberId}/{uuid}.{ext}` |
| `photo_lab_image` | public | `labs/{photoLabId}/images/{uuid}.{ext}` |
| `post_image` | public | `posts/{postId}/{uuid}.{ext}` |
| `scanned_photo` | private | `orders/{developmentOrderId}/scans/{uuid}.{ext}` |
| `photo_restoration` | private | `restorations/{memberId}/{original\|restored}/{uuid}.{ext}` |
| `photo_lab_document` | private | `labs/{photoLabId}/documents/{documentType}/{uuid}.{ext}` |
| 임시 업로드 | public | `temp/{memberId}/{uuid}.{ext}` |

---

## Compute Engine

| 항목 | 값 |
|------|-----|
| 인스턴스 이름 | `finders-server` |
| 머신 유형 | e2-medium |
| vCPU | 2 vCPU |
| RAM | 4 GB |
| 부팅 디스크 | Ubuntu 22.04 LTS, 20GB |
| 리전/영역 | asia-northeast3-a (서울) |
| **외부 IP (고정)** | **34.50.19.146** |
| 내부 IP | 10.178.0.2 |
| 방화벽 | HTTP(80), HTTPS(443) 허용 |
| **예상 비용** | **~$34/월** |

---

## 네트워크 구성

```
[클라이언트]
    ↓ (HTTPS)
[Compute Engine: 34.50.19.146]
    ↓ (MySQL 3306)
[Cloud SQL: 34.64.50.136]

[Compute Engine] → [Cloud Storage: finders-public]   (공개 이미지)
                 → [Cloud Storage: finders-private]  (비공개 파일, Signed URL)
```

### 방화벽 규칙
| 포트 | 용도 | 상태 |
|------|------|------|
| 80 | HTTP | 허용 |
| 443 | HTTPS | 허용 |
| 8080 | Spring Boot | **추가 필요** |

---

## 예상 월 비용

| 서비스 | 월 비용 |
|--------|---------|
| Cloud SQL | ~$27 |
| Compute Engine | ~$34 |
| Cloud Storage | ~$1 |
| **총합** | **~$62/월** |

---

## 무료 크레딧 정보

| 항목 | 값 |
|------|-----|
| 무료 크레딧 | $300 |
| 유효 기간 | 90일 |
| 시작일 | 2024.12.28 |
| **만료 예정일** | **2025.03.28** |
| 예상 사용량 | ~$62/월 × 3개월 = ~$186 |

### 무료 기간 종료 후
- **2025년 3월 28일**부터 월 **~$62** 과금 예정
- 필요시 머신 스펙 다운그레이드로 비용 절감 가능:
  - Compute Engine: e2-medium → e2-small (~$17/월)
  - Cloud SQL: 1 vCPU → db-f1-micro (~$10/월)

---

## 환경별 접속 정보

### 로컬 개발 (Docker MySQL)
```
Host: localhost
Port: 3306
Database: finders
User: finders
Password: finders123
```

### 배포 서버 (Cloud SQL)
```
Host: 34.64.50.136
Port: 3306
Database: finders
User: finders
Password: [.env.prod 참조]
```

### 배포 서버 SSH 접속
```bash
gcloud compute ssh finders-server --zone=asia-northeast3-a
# 또는
ssh [사용자]@34.50.19.146
```
