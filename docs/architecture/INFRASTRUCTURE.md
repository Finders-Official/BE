# Infrastructure

## 환경 분리 구조

| 환경 | 브랜치 | 도메인 | 배포 방식 | Cloud SQL DB | 용도 |
|------|--------|--------|-----------|--------------|------|
| **Local** | - | localhost:8080 | Docker Compose | Docker MySQL | 로컬 개발 |
| **Dev** | develop | dev-api.finders.it.kr | Blue-Green (Traefik) | finders_dev | FE 연동 테스트 |
| **Prod** | main | api.finders.it.kr | Blue-Green (Traefik) | finders | 데모데이, 실제 운영 |

### 아키텍처 다이어그램

```
                    Cloudflare Zero Trust
                           │
          ┌────────────────┴────────────────┐
          ▼                                 ▼
   api.finders.it.kr              dev-api.finders.it.kr
          └────────────────┬────────────────┘
                           │
                Cloudflare Tunnel (1개, 통합)
                           │
                      cloudflared
                           │
                        Traefik
                     (Host 라우팅)
                 ┌─────────┴─────────┐
                 ▼                   ▼
          Prod Blue/Green      Dev Blue/Green
                 │                   │
                 ▼                   ▼
            finders-redis      finders-redis-dev
                 │                   │
                 ▼                   ▼
┌─────────────────────────────────────────────────────┐
│              Cloud SQL (finders-db)                 │
│  ┌─────────────────┐       ┌─────────────────┐     │
│  │     finders     │       │   finders_dev   │     │
│  │    (prod DB)    │       │    (dev DB)     │     │
│  └─────────────────┘       └─────────────────┘     │
└─────────────────────────────────────────────────────┘
```

### 브랜치 전략

```
feature/* → develop (PR) → dev 환경 자동 배포
develop → main (PR) → prod 환경 자동 배포
```

## 배포 방식

### Blue-Green 무중단 배포

Traefik 리버스 프록시를 통한 Blue-Green 배포로 **다운타임 0초**를 달성합니다.

**배포 흐름**:
1. 현재 활성 슬롯(Blue) 감지
2. 비활성 슬롯(Green)에 새 버전 배포
3. Green 슬롯 헬스체크 (직접 컨테이너 exec)
4. 헬스체크 통과 시 Traefik이 자동으로 트래픽을 Green으로 전환
5. Blue 슬롯 중지 및 제거
6. 헬스체크 실패 시 Green 제거, Blue 유지 (자동 롤백)

**Docker Compose 구조**:
- `docker-compose.yml`: 로컬 개발용 (MySQL, Redis)
- `docker-compose.infra.yml`: 공통 인프라 (Traefik, cloudflared, Redis) — 서버 배포용
- `docker-compose.prod.yml`: Prod Blue/Green 서비스
- `docker-compose.dev.yml`: Dev Blue/Green 서비스

**배포 명령어 예시**:
```bash
# Prod Blue 슬롯 배포
docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml --profile blue up -d

# Prod Green 슬롯 배포 (Blue-Green 전환)
docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml --profile green up -d
docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml --profile blue down
```

**Traefik 라우팅**:
- Host 헤더 기반 자동 라우팅
- `api.finders.it.kr` → Prod Blue 또는 Green
- `dev-api.finders.it.kr` → Dev Blue 또는 Green
- Docker label로 설정, 재시작 불필요

---

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
| 공개 IP | 비활성화됨 |
| Private IP | `10.68.240.3` |
| 포트 | 3306 |
| 백업 | 자동 |
| 네트워크 | finders-vpc (VPC 내부 통신 전용) |
| **예상 비용** | **~$27/월** |

### 접속 정보

> **주의**: Cloud SQL 공개 IP가 비활성화되어 **IAP 터널**을 통해서만 접속 가능합니다.

**IAP 터널 접속 방법**
```bash
# 1. 터널 열기 (로컬 3307 → Cloud SQL 3306)
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap \
  -- -L 3307:10.68.240.3:3306

# 2. DB 클라이언트에서 연결
Host: localhost
Port: 3307
```

**Prod 환경**
```
Database: finders
User: finders
Password: [.env.prod 참조]
```

**Dev 환경**
```
Database: finders_dev
User: finders
Password: [.env.dev 참조]
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
| 인스턴스 이름 | `finders-server-v2` |
| 머신 유형 | e2-medium |
| vCPU | 2 vCPU |
| RAM | 4 GB |
| 부팅 디스크 | Ubuntu 22.04 LTS, 20GB |
| 리전/영역 | asia-northeast3-a (서울) |
| 외부 IP | 없음 (IAP 터널 사용) |
| 내부 IP | `10.0.2.2` |
| VPC/서브넷 | `finders-vpc` / `private-app-subnet` |
| 네트워크 태그 | `api-server`, `http-server`, `https-server` |
| SSH 접속 | IAP 터널 필수 |
| **예상 비용** | **~$34/월** |

### SSH 접속 방법

```bash
# IAP 터널을 통한 SSH 접속
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap
```

---

## Cloudflare

| 항목 | 값 |
|------|-----|
| 서비스 | Zero Trust (구 Cloudflare Access) |
| 도메인 | finders.it.kr (가비아) |
| 플랜 | Free (50명까지) |
| 기능 | DDoS 방어, WAF, SSL, Tunnel |

### Cloudflare Tunnel (통합)

**단일 터널 구조**:
- 터널 이름: `finders-tunnel` (통합)
- 토큰: `TUNNEL_TOKEN` (prod/dev 공용)
- Public Hostnames:
  - `api.finders.it.kr` → `http://traefik:80`
  - `dev-api.finders.it.kr` → `http://traefik:80`

**장점**:
- 관리 포인트 단일화 (터널 1개, 토큰 1개)
- Traefik이 Host 헤더로 prod/dev 자동 라우팅
- 환경변수 단순화 (`TUNNEL_TOKEN_PROD`, `TUNNEL_TOKEN_DEV` → `TUNNEL_TOKEN`)

**특징**:
- Public IP 직접 노출 없음
- DDoS 무제한 방어
- 자동 SSL 인증서
- 접속 로그 자동 기록

---

## 네트워크 구성

### 현재 구성 (Cloudflare Tunnel 사용)

```
[클라이언트]
    ↓ HTTPS
[Cloudflare Edge Network] (전 세계 데이터센터)
    ↓ 암호화된 터널
[cloudflared 데몬] (Compute Engine 내부)
    ↓
[Spring Boot :8080]
    ↓ MySQL 3306 (VPC 내부 Private IP)
[Cloud SQL: 10.68.240.3]

[Compute Engine] → [Cloud Storage: finders-public]   (공개 이미지)
                 → [Cloud Storage: finders-private]  (비공개 파일, Signed URL)
```

### 방화벽 규칙

| 포트 | 용도 | 상태 | 비고 |
|------|------|------|------|
| 22 | SSH | 허용 (제한적) | 관리자 IP만 |
| 80 | HTTP | **차단 권장** | Cloudflare Tunnel 사용 시 불필요 |
| 443 | HTTPS | **차단 권장** | Cloudflare Tunnel 사용 시 불필요 |
| 3306 | MySQL | 차단 | 내부 통신만 |
| 8080 | Spring Boot | 차단 | 내부 통신만 |

**⚠️ 주의**: Cloudflare Tunnel은 outbound 연결만 사용하므로, 인바운드 포트 오픈 불필요

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

### Dev/Prod 환경 (Cloud SQL via IAP 터널)

> Cloud SQL 공개 IP 비활성화됨. IAP 터널 필수!

```bash
# 1. IAP 터널 열기
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap \
  -- -L 3307:10.68.240.3:3306

# 2. IntelliJ/DBeaver 등 DB 클라이언트에서 연결
Host: localhost
Port: 3307
Database: finders_dev (또는 finders)
User: finders
Password: [.env.dev/.env.prod 참조]
```

| 환경 | Database | API URL |
|------|----------|---------|
| Dev | finders_dev | https://dev-api.finders.it.kr |
| Prod | finders | https://api.finders.it.kr |

### 배포 서버 SSH 접속

> **주의**: 외부 IP가 없으므로 IAP 터널 필수!

```bash
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap
```

### Docker 컨테이너 관리

**Prod 환경**:
```bash
# 현재 실행 중인 슬롯 확인
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml ps

# Blue 슬롯 시작
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml --profile blue up -d

# Green 슬롯 시작 (Blue-Green 전환)
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml --profile green up -d

# Blue 슬롯 중지
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml --profile blue down
```

**Dev 환경**:
```bash
# Dev Blue 슬롯 시작
sudo docker compose -f docker-compose.infra.yml -f docker-compose.dev.yml --profile blue up -d

# Dev Green 슬롯 시작
sudo docker compose -f docker-compose.infra.yml -f docker-compose.dev.yml --profile green up -d
```

**로그 확인**:
```bash
# Prod 활성 슬롯 로그
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml logs -f

# Traefik 로그
sudo docker compose -f docker-compose.infra.yml logs traefik -f
```

# 컨테이너 상태 확인
sudo docker ps
```
