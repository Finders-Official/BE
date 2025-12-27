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
| 머신 유형 | db-custom-1-3840 |
| vCPU | 1 vCPU |
| RAM | 3.75 GB |
| 저장용량 | 10 GB SSD |
| 가용성 | 단일 영역 |
| 공개 IP | `34.64.50.136` |
| 포트 | 3306 |
| 백업 | 자동 |
| 승인된 네트워크 | `34.50.19.146/32` (finders-server) |
| **예상 비용** | **~$65/월** |

### 접속 정보
```
Host: 34.64.50.136
Port: 3306
Database: finders
User: finders
```

---

## Cloud Storage

| 항목 | 값 |
|------|-----|
| 버킷 이름 | `finders-storage` |
| 리전 | asia-northeast3 (서울) |
| 스토리지 클래스 | Standard |
| 용도 | 이미지, 사업자등록증 저장 |
| **예상 비용** | **~$1/월** (5GB 무료) |

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

[Compute Engine] → [Cloud Storage: finders-storage]
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
| Cloud SQL | ~$65 |
| Compute Engine | ~$34 |
| Cloud Storage | ~$1 |
| **총합** | **~$100/월** |

---

## 무료 크레딧 정보

| 항목 | 값 |
|------|-----|
| 무료 크레딧 | $300 |
| 유효 기간 | 90일 |
| 시작일 | 2024.12.28 |
| **만료 예정일** | **2025.03.28** |
| 예상 사용량 | ~$100/월 × 3개월 = ~$300 |

### 무료 기간 종료 후
- **2025년 3월 28일**부터 월 **~$100** 과금 예정
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
