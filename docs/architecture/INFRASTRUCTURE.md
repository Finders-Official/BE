# Infrastructure

## 환경 분리 구조

| 환경 | 브랜치 | 도메인 | 배포 방식 | Database | Redis | 용도 |
|------|--------|--------|-----------|----------|-------|------|
| **Local** | - | localhost:8080 | Docker Compose | Docker MySQL | Docker Redis | 로컬 개발 |
| **Dev** | develop | dev-api.finders.it.kr | Blue-Green (Traefik) | Cloud SQL `finders_dev` | Docker Redis (GCE 내) | FE 연동 테스트 |
| **Prod** | main | api.finders.it.kr | Blue-Green (Traefik) | Cloud SQL `finders` | Upstash Redis (외부) | 데모데이, 실제 운영 |

### 브랜치 전략

```
feature/* → develop (PR) → dev 환경 자동 배포
develop → main (PR) → prod 환경 자동 배포
```

---

## 아키텍처 다이어그램

```
                        Internet
                           │
                    Cloudflare Edge
                  (DDoS, WAF, SSL, DNS)
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
       api.finders.it.kr      dev-api.finders.it.kr
              └────────────┬────────────┘
                           │
                Cloudflare Tunnel (통합)
                           │
    ═══════════════════════╪══════════════════════════════
    GCP  (asia-northeast3) │  VPC: finders-vpc
    ═══════════════════════╪══════════════════════════════
                           │
    ┌──────────────────────┼──────────────────────────┐
    │  private-app-subnet  │  (10.0.2.0/24)           │
    │                      ▼                          │
    │              ┌─── GCE finders-server-v2 ───┐    │
    │              │   (e2-medium, 10.0.2.2)     │    │
    │              │                             │    │
    │              │   cloudflared ──► Traefik   │    │
    │              │              (Host 라우팅)   │    │
    │              │         ┌────────┴────────┐ │    │
    │              │         ▼                 ▼ │    │
    │              │   prod-blue/green   dev-blue/green│
    │              │         │                 │ │    │
    │              │         │          redis-dev│    │
    │              │         │                   │    │
    │              └─────────┼───────────────────┘    │
    │                        │                        │
    │  Cloud NAT ◄─── Cloud Router (finders-router)   │
    │  (finders-nat)   Static IP: 34.64.198.84       │
    └────────────────────────┼────────────────────────┘
                             │
    ┌────────────────────────┼────────────────────────┐
    │  private-db-subnet     │  (10.0.3.0/24)         │
    │                        ▼                        │
    │              Cloud SQL (finders-db)              │
    │          MySQL 8.0 │ Private IP: 10.68.240.3    │
    │       ┌─────────────┴─────────────┐             │
    │       │    finders (prod)         │             │
    │       │    finders_dev (dev)      │             │
    │       └───────────────────────────┘             │
    └─────────────────────────────────────────────────┘

    ┌─ GCP 관리형 서비스 (VPC 외부) ──────────────────┐
    │                                                  │
    │  Cloud Storage          Artifact Registry        │
    │  ├─ finders-public      ├─ finders-docker (API)  │
    │  └─ finders-private     └─ finders-image (resizer)│
    │                                                  │
    │  Cloud Run              Secret Manager           │
    │  └─ img-resizer         ├─ finders-prod-config   │
    │                         └─ finders-dev-config    │
    │                                                  │
    │  Monitoring Dashboard   Cloud Logging (gcplogs)  │
    └──────────────────────────────────────────────────┘

    ┌─ 외부 서비스 ────────────────────────────────────┐
    │  Upstash Redis (prod)   GitHub Actions (CI/CD)   │
    │  Cloudflare (DNS/CDN)   가비아 (도메인 등록)       │
    └──────────────────────────────────────────────────┘
```

---

## GCP 프로젝트 정보

| 항목 | 값 |
|------|-----|
| 프로젝트 | My First Project |
| 프로젝트 ID | project-37afc2aa-d3d3-4a1a-8cd |
| 리전 | asia-northeast3 (서울) |

---

## Terraform Infrastructure (IaC)

인프라를 코드로 관리(IaC)하기 위해 Terraform을 사용합니다. State는 `gs://finders-terraform-state/`에 저장됩니다.

### 모듈 구조

```
infra/
├── main.tf              # 모듈 오케스트레이션
├── variables.tf         # 전역 변수
├── iam.tf               # 3-tier IAM + 서비스 계정
├── cloudrun.tf          # Cloud Run img-resizer
├── moved.tf             # import 블록 (기존 리소스 가져오기)
└── modules/
    ├── networking/      # VPC, 서브넷, 방화벽, Cloud Router, Cloud NAT
    ├── compute/         # GCE 인스턴스
    ├── database/        # Cloud SQL
    ├── storage/         # GCS 버킷
    └── cloudflare/      # Cloudflare Tunnel
```

### 관리 대상 리소스

| 카테고리 | 리소스 |
|----------|--------|
| **Networking** | VPC (`finders-vpc`), 3 Subnets, 6 Firewall Rules, Cloud Router, Cloud NAT, Static IP |
| **Compute** | GCE (`finders-server-v2`) |
| **Database** | Cloud SQL (`finders-db`), 2 Databases (`finders`, `finders_dev`) |
| **Storage** | GCS (`finders-public`, `finders-private`) |
| **Registry** | Artifact Registry (`finders-docker`, `finders-image`) |
| **Serverless** | Cloud Run (`img-resizer`) |
| **Security** | Secret Manager (2), WIF Pool + Provider, IAM Bindings |
| **Monitoring** | Cloud Monitoring Dashboard |
| **External** | Cloudflare Tunnel |

### CI/CD (Terraform)

- **PR 생성 시**: `terraform plan` 자동 실행 → PR 코멘트로 결과 표시
- **develop 머지 시**: `terraform apply` 자동 실행
- **인증**: Workload Identity Federation (WIF) — `terraform-ci` SA
- **Workflow**: `.github/workflows/terraform.yml`

> ⚠️ 로컬에서 `terraform apply` 금지 (CI/CD만 사용)

---

## IAM & Service Accounts

### 3-tier IAM 구조

| Tier | 대상 | 주요 권한 |
|------|------|-----------|
| **Admin** | sachi009955@gmail.com | 프로젝트 Owner, 전체 관리 |
| **Lead** | wldy4627@gmail.com | Compute Admin, Secret Manager Admin, Storage Admin |
| **Team** | 팀원 8명 | Secret Manager Accessor (Read-only), Monitoring Viewer, Logging Viewer |

### Service Accounts

| SA | 용도 | 주요 권한 |
|---|------|----------|
| **compute-sa** | GCE 메인 서버 | Cloud SQL Client, Secret Manager Accessor, Storage Admin, AR Reader |
| **img-resizer** | Cloud Run 이미지 리사이저 | Storage Object Admin (public bucket), Logging Writer |
| **terraform-ci** | CI/CD Terraform (WIF) | 전체 인프라 관리 권한 |

### Workload Identity Federation (WIF)

GitHub Actions가 GCP에 접근할 때 서비스 계정 키 없이 인증하는 방식입니다.

- **Pool**: `finders-pool`
- **Provider**: `github-provider`
- **Service Account**: `terraform-ci@project-37afc2aa-d3d3-4a1a-8cd.iam.gserviceaccount.com`
- **조건**: `Finders-Official/BE` 리포지토리의 GitHub Actions만 허용

---

## VPC 네트워크

### 서브넷 구성

| 서브넷 | CIDR | 용도 |
|--------|------|------|
| `public-subnet` | 10.0.1.0/24 | (예약 — 현재 미사용) |
| `private-app-subnet` | 10.0.2.0/24 | GCE 인스턴스 |
| `private-db-subnet` | 10.0.3.0/24 | Cloud SQL (PSA Peering) |

### 방화벽 규칙

| 규칙 | 방향 | 포트 | 대상 태그 | 설명 |
|------|------|------|-----------|------|
| `allow-iap-ssh` | Ingress | 22 | `api-server` | IAP 터널 SSH (`35.235.240.0/20`) |
| `allow-internal` | Ingress | all | (VPC 전체) | VPC 내부 통신 (`10.0.0.0/8`) |
| `allow-health-check` | Ingress | 80,443 | `http-server` | GCP Health Check (`35.191.0.0/16`, `130.211.0.0/22`) |
| `allow-http` | Ingress | 80 | `http-server` | HTTP (모든 소스) |
| `allow-https` | Ingress | 443 | `https-server` | HTTPS (모든 소스) |
| `deny-all-ingress` | Ingress | all | (전체) | 기본 차단 (최저 우선순위) |

> **참고**: Cloudflare Tunnel은 outbound 연결만 사용하므로 인바운드 포트 오픈이 실질적으로 불필요하지만, Health Check와 IAP SSH를 위해 유지됩니다.

### VPC Peering (Private Service Access)

Cloud SQL은 Google이 관리하는 별도 VPC에 존재합니다. `finders-vpc`와 Google 서비스 VPC를 **PSA (Private Service Access)** 방식으로 피어링하여, 공인 IP 없이 내부 IP(`10.68.240.3`)로 DB에 접근합니다.

| 항목 | 값 |
|------|-----|
| 피어링 유형 | PSA (Private Service Access) |
| 예약 IP 대역 | `10.68.240.0/20` |
| 연결 서비스 | `servicenetworking.googleapis.com` |
| 대상 | Cloud SQL (`finders-db`) → Private IP `10.68.240.3` |

### Private IP Google Access

각 서브넷에 `private_ip_google_access = true`가 설정되어 있어, GCE가 외부 IP 없이도 GCS, Artifact Registry, Secret Manager 등 Google API에 접근할 수 있습니다.

### Cloud NAT

GCE 인스턴스에 외부 IP가 없으므로, Docker 이미지 pull 등 **Google API 외의** 아웃바운드 인터넷 접근(Docker Hub, Upstash Redis 등)을 위해 Cloud NAT를 사용합니다.

| 항목 | 값 |
|------|-----|
| Cloud Router | `finders-router` |
| Cloud NAT | `finders-nat` |
| 고정 IP | `34.64.198.84` (`finders-nat-ip`) |
| NAT IP 할당 | MANUAL_ONLY (고정 IP 사용) |
| 서브넷 범위 | ALL_SUBNETWORKS_ALL_IP_RANGES |

---

## Compute Engine

| 항목 | 값 |
|------|-----|
| 인스턴스 이름 | `finders-server-v2` |
| 머신 유형 | e2-medium |
| vCPU / RAM | 2 vCPU / 4 GB |
| 부팅 디스크 | Ubuntu 22.04 LTS, 20GB |
| 리전/영역 | asia-northeast3-a (서울) |
| 외부 IP | 없음 (Cloud NAT로 아웃바운드, IAP로 SSH) |
| 내부 IP | `10.0.2.2` |
| VPC/서브넷 | `finders-vpc` / `private-app-subnet` |
| 네트워크 태그 | `api-server`, `http-server`, `https-server` |
| 서비스 계정 | `compute-sa` |
| 로깅 | Prod: Docker `gcplogs` → Cloud Logging / Dev: `json-file` 로컬 저장 |

### GCE 내 Docker 컨테이너 구성

```
┌─ docker-compose.infra.yml ───────────────────┐
│  traefik        (리버스 프록시, CORS, 라우팅)   │
│  cloudflared    (Cloudflare Tunnel 클라이언트)  │
│  redis-dev      (Dev 환경 Redis, 비밀번호 없음) │
└──────────────────────────────────────────────┘

┌─ docker-compose.prod.yml ────────────────────┐
│  prod-blue  또는  prod-green  (Spring Boot)   │
└──────────────────────────────────────────────┘

┌─ docker-compose.dev.yml ─────────────────────┐
│  dev-blue   또는  dev-green   (Spring Boot)   │
└──────────────────────────────────────────────┘
```

### SSH 접속 방법

```bash
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap
```

---

## 배포 방식 (Blue-Green)

Traefik 리버스 프록시를 통한 Blue-Green 배포로 **다운타임 0초**를 달성합니다.

### 배포 흐름

1. 현재 활성 슬롯(Blue) 감지
2. 비활성 슬롯(Green)에 새 버전 배포
3. Green 슬롯 헬스체크 (직접 컨테이너 exec)
4. 헬스체크 통과 시 Traefik이 자동으로 트래픽을 Green으로 전환
5. Blue 슬롯 중지 및 제거
6. 헬스체크 실패 시 Green 제거, Blue 유지 (자동 롤백)

### Traefik 라우팅

- Host 헤더 기반 자동 라우팅
- `api.finders.it.kr` → Prod Blue 또는 Green
- `dev-api.finders.it.kr` → Dev Blue 또는 Green
- Docker label로 설정, 재시작 불필요

---

## CI/CD 파이프라인 (GitHub Actions)

### 워크플로우 구성

| Workflow | 트리거 | 동작 |
|----------|--------|------|
| `ci.yml` | PR to develop/main | Build + Test (Gradle) |
| `cd-dev.yml` | develop merge | AR push → GCE SSH → Blue-Green 배포 (dev) |
| `cd-prod.yml` | main merge | AR push → GCE SSH → Blue-Green 배포 (prod) |
| `terraform.yml` | PR (infra/ 변경) / develop merge | Plan (PR) / Apply (merge) |
| `img-resizer.yml` | main merge (resizer 변경) | Cloud Run 배포 |

### 인증 흐름

```
GitHub Actions → WIF (finders-pool/github-provider) → terraform-ci SA → GCP API
```

- GitHub Secrets에는 WIF 관련 3개만 저장: `WIF_PROVIDER`, `WIF_SERVICE_ACCOUNT`, `GCP_PROJECT_ID`
- 앱 설정값(DB, JWT, OAuth 등)은 GCP Secret Manager에서 가져옴

---

## Cloud SQL (MySQL)

| 항목 | 값 |
|------|-----|
| 인스턴스 ID | `finders-db` |
| 버전 | MySQL 8.0 |
| Cloud SQL 버전 | Enterprise |
| 머신 유형 | db-g1-small (공유 vCPU, 1.7 GB RAM) |
| 저장용량 | 10 GB SSD |
| 가용성 | 단일 영역 |
| 공개 IP | 비활성화됨 |
| Private IP | `10.68.240.3` (VPC Peering — PSA) |
| 포트 | 3306 |
| 백업 | 자동 |

### 접속 방법 (IAP 터널)

Cloud SQL에 공개 IP가 없으므로 IAP 터널을 통해서만 접속 가능합니다.

```bash
# 1. 터널 열기 (로컬 3307 → Cloud SQL 3306)
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap \
  -- -L 3307:10.68.240.3:3306

# 2. DB 클라이언트 연결
Host: localhost
Port: 3307
Database: finders_dev (또는 finders)
User: finders
Password: [Secret Manager 참조]
```

---

## Redis

| 환경 | 서비스 | 호스트 | 인증 |
|------|--------|--------|------|
| **Prod** | Upstash Redis | `discrete-anchovy-36474.upstash.io:6379` | TLS + 비밀번호 (Secret Manager) |
| **Dev** | Docker Redis | `finders-redis-dev:6379` (GCE 내부) | 없음 |
| **Local** | Docker Redis | `localhost:6379` | 없음 |

> Prod Redis가 Upstash(외부 관리형)인 이유: GCP Memorystore의 최소 티어가 비용 대비 과도하여 Upstash Free 티어 사용

---

## Cloud Storage

### 버킷 구성

| 버킷 | 용도 | 접근 방식 |
|------|------|----------|
| `finders-public` | 공개 이미지 (프로필, 현상소, 게시글) | 직접 URL (`allUsers:objectViewer`) |
| `finders-private` | 비공개 파일 (스캔 사진, 서류, AI 복원) | Signed URL |

### 공통 설정

| 항목 | 값 |
|------|-----|
| 리전 | asia-northeast3 (서울) |
| 스토리지 클래스 | Standard |
| 액세스 제어 | 균일 (Uniform) |
| CORS | 특정 Origin만 허용 (`api.finders.it.kr`, `dev-api.finders.it.kr`, `localhost:3000` 등) |
| Lifecycle | `temp/` 경로 30일 후 자동 삭제 |

### 경로 규칙

| 용도 | 버킷 | 경로 패턴 |
|------|------|----------|
| 프로필 사진 | public | `profiles/{memberId}/{uuid}.{ext}` |
| 현상소 이미지 | public | `labs/{photoLabId}/images/{uuid}.{ext}` |
| 게시글 이미지 | public | `posts/{postId}/{uuid}.{ext}` |
| 스캔 사진 | private | `orders/{developmentOrderId}/scans/{uuid}.{ext}` |
| AI 복원 사진 | private | `restorations/{memberId}/{original\|restored}/{uuid}.{ext}` |
| 현상소 서류 | private | `labs/{photoLabId}/documents/{documentType}/{uuid}.{ext}` |
| 임시 업로드 | public | `temp/{memberId}/{uuid}.{ext}` |

---

## Artifact Registry

Docker 이미지 저장소입니다. Docker Hub 대신 GCP AR을 사용하여 WIF 인증과 연동됩니다.

| 레지스트리 | 용도 | 이미지 |
|-----------|------|--------|
| `finders-docker` | API 서버 이미지 | `asia-northeast3-docker.pkg.dev/.../finders-docker/finders-api` |
| `finders-image` | img-resizer 이미지 | `asia-northeast3-docker.pkg.dev/.../finders-image/img-resizer` |

---

## Cloud Run (img-resizer)

GCS에 업로드된 이미지를 리사이징하는 서버리스 서비스입니다.

| 항목 | 값 |
|------|-----|
| 서비스 이름 | `img-resizer` |
| 리전 | asia-northeast3 |
| 서비스 계정 | `img-resizer@...` |
| 이미지 소스 | Artifact Registry (`finders-image`) |

---

## Cloudflare

| 항목 | 값 |
|------|-----|
| 서비스 | Zero Trust |
| 도메인 | finders.it.kr (가비아 등록, Cloudflare DNS) |
| 플랜 | Free |
| 기능 | DDoS 방어, WAF, SSL, Tunnel |

### Cloudflare Tunnel (통합)

단일 터널로 prod/dev를 모두 처리합니다.

- **터널 이름**: `finders-tunnel`
- **토큰**: `TUNNEL_TOKEN` (prod/dev 공용)
- **Public Hostnames**:
  - `api.finders.it.kr` → `http://traefik:80`
  - `dev-api.finders.it.kr` → `http://traefik:80`
- Traefik이 Host 헤더로 prod/dev 자동 라우팅

**특징**:
- Public IP 직접 노출 없음 (GCE 외부 IP 없음)
- DDoS 무제한 방어
- 자동 SSL 인증서
- 접속 로그 자동 기록

---

## Monitoring & Logging

| 서비스 | 용도 |
|--------|------|
| **Cloud Logging** | Prod 컨테이너 로그 (`gcplogs` 드라이버로 자동 전송, Dev는 `json-file` 로컬 저장) |
| **Cloud Monitoring** | 커스텀 대시보드 (Terraform 관리) |

---

## 예상 월 비용

| 서비스 | 월 비용 |
|--------|---------|
| Cloud SQL (db-g1-small) | ~$27 |
| Compute Engine (e2-medium) | ~$34 |
| Cloud Storage | ~$1 |
| Cloud NAT | ~$1 |
| Cloud Run (img-resizer) | ~$0 (Free Tier) |
| Artifact Registry | ~$0 (Free Tier) |
| Upstash Redis | ~$0 (Free Tier) |
| Cloudflare | $0 (Free Plan) |
| **총합** | **~$63/월** |

---

## 환경별 접속 정보

### 로컬 개발 (Docker)

```
Host: localhost
Port: 3306
Database: finders
User: finders
Password: finders123
```

### Dev/Prod 환경 (Cloud SQL via IAP 터널)

| 환경 | Database | API URL |
|------|----------|---------|
| Dev | finders_dev | https://dev-api.finders.it.kr |
| Prod | finders | https://api.finders.it.kr |

비밀번호는 GCP Secret Manager의 `finders-prod-config` / `finders-dev-config`에서 관리됩니다.

---

## Docker 컨테이너 관리

### 상태 확인

```bash
# SSH 접속 후
sudo docker ps

# Prod 슬롯 확인
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml ps

# Dev 슬롯 확인
sudo docker compose -f docker-compose.infra.yml -f docker-compose.dev.yml ps
```

### 로그 확인

```bash
# Prod 활성 슬롯 로그
sudo docker compose -f docker-compose.infra.yml -f docker-compose.prod.yml logs -f

# Traefik 로그
sudo docker compose -f docker-compose.infra.yml logs traefik -f
```

---

**마지막 업데이트**: 2026-02-11
