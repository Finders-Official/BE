# 인프라 문서 인덱스

> Finders API 서버의 인프라 운영 문서 모음

## 문서 목록

| 문서 | 설명 | 대상 |
|------|------|------|
| [INFRASTRUCTURE.md](../architecture/INFRASTRUCTURE.md) | **전체 인프라 아키텍처** (VPC, GCE, Cloud SQL, GCS, Cloud Run, CI/CD 등) | 모든 개발자 |
| [SECRET_MANAGEMENT.md](./SECRET_MANAGEMENT.md) | GCP Secret Manager 관리 가이드 | 모든 개발자 |
| [GCP_LOGGING_GUIDE.md](./GCP_LOGGING_GUIDE.md) | GCP Cloud Logging 확인 가이드 | DevOps |
| [IAC_TERRAFORM_INTRO.md](./IAC_TERRAFORM_INTRO.md) | IaC/Terraform 개념 학습 | 모든 개발자 |
| [TERRAFORM_OPERATIONS.md](./TERRAFORM_OPERATIONS.md) | Terraform 운영 가이드 (plan/apply, 안전 수칙) | DevOps |
| [GCP_PROJECT_MIGRATION_RUNBOOK.md](./GCP_PROJECT_MIGRATION_RUNBOOK.md) | GCP 프로젝트 마이그레이션 런북 (Secrets 기반 전환) | DevOps |

---

## Terraform 운영

### 개요

Finders 인프라는 **전량 Terraform으로 코드화**되어 있습니다. 모든 인프라 변경은 PR → CI/CD를 통해 관리됩니다.

### 관리 대상 리소스

| 카테고리 | 리소스 |
|----------|--------|
| Networking | VPC, 3 Subnets, 6 Firewall Rules, Cloud Router, Cloud NAT, Static IP |
| Compute | GCE (`finders-server`) |
| Database | Cloud SQL (`finders-db`), 2 DBs |
| Storage | GCS (`finders-487717-public`, `finders-487717-private`) |
| Registry | Artifact Registry (`finders-docker`, `finders-image`) |
| Serverless | Cloud Run (`img-resizer`) |
| Security | Secret Manager (2), WIF Pool + Provider, IAM Bindings |
| Monitoring | Cloud Monitoring Dashboard |
| External | Cloudflare Tunnel |

### 빠른 시작

```bash
# 1. Terraform 설치 (1.5.0+)
brew install terraform

# 2. GCP 인증
gcloud auth application-default login

# 3. 변수 설정
cd infra
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 편집

# 4. 초기화 & Plan
terraform init
terraform plan  # No changes 확인
```

### CI/CD

- **PR 생성 시**: `terraform plan` 자동 실행 → PR 코멘트로 결과 표시
- **develop 머지 시**: `terraform apply` 자동 실행
- **Workflow**: `.github/workflows/terraform.yml`

### 주의사항

- ⚠️ 로컬에서 `terraform apply` 금지 (CI/CD만 사용)
- ⚠️ `prevent_destroy` 제거 금지
- ⚠️ `terraform.tfvars` 커밋 금지

---

## 빠른 링크

### 외부 대시보드
- [GCP Console](https://console.cloud.google.com/)
- [Cloudflare Dashboard](https://dash.cloudflare.com/)
- [Upstash Console](https://console.upstash.com/)
- [가비아 DNS 관리](https://customer.gabia.com/)

### 상태 페이지
- [GCP Status](https://status.cloud.google.com/)
- [Cloudflare Status](https://www.cloudflarestatus.com/)

---

## 긴급 상황

### 서비스 장애 시 순서

1. [Health Check](https://api.finders.it.kr/health) 확인
2. [GCP Console](https://console.cloud.google.com/) — GCE, Cloud SQL 상태 확인
3. [Cloudflare Dashboard](https://dash.cloudflare.com/) — Tunnel 상태 확인
4. SSH 접속 후 Docker 상태 확인
5. 팀에 알림

### 자주 발생하는 문제

| 증상 | 원인 | 해결 |
|------|------|------|
| 사이트 접속 안 됨 | Cloudflare Tunnel 중단 | SSH 후 `sudo docker compose -f docker-compose.infra.yml restart cloudflared` |
| DB 연결 실패 | Cloud SQL 중단 | GCP Console에서 인스턴스 확인 |
| SSH 접속 안 됨 | IAP 권한 또는 GCE 중지 | GCP Console에서 인스턴스/IAM 확인 |
| Docker 이미지 pull 실패 | Cloud NAT 문제 | GCP Console에서 NAT 상태 확인 |

---

## 파일 구조

```
docs/
├─ infra/
│   ├─ README.md                  (이 파일)
│   ├─ SECRET_MANAGEMENT.md       (비밀 정보 관리)
│   ├─ GCP_LOGGING_GUIDE.md       (로깅)
│   ├─ IAC_TERRAFORM_INTRO.md     (IaC/Terraform 개념)
│   ├─ TERRAFORM_OPERATIONS.md    (Terraform 운영)
│   └─ GCP_PROJECT_MIGRATION_RUNBOOK.md (프로젝트 마이그레이션 런북)
│
└─ architecture/
    ├─ INFRASTRUCTURE.md          (전체 인프라 아키텍처)
    ├─ ARCHITECTURE.md            (애플리케이션 구조)
    └─ ERD.md                     (데이터베이스)
```

---

**마지막 업데이트**: 2026-02-11
