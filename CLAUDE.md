# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Finders API - 필름 현상소 연결 플랫폼 백엔드. Java 21 + Spring Boot 3.4 + MySQL 8.x 기반.

## Documentation References

| 목적 | 참조 문서 |
|------|----------|
| **코드 작성** | [docs/development/CODE_STYLE.md](docs/development/CODE_STYLE.md) |
| **Git (브랜치/커밋)** | [docs/development/CONVENTIONS.md](docs/development/CONVENTIONS.md) |
| **인프라 아키텍처** | [docs/architecture/INFRASTRUCTURE.md](docs/architecture/INFRASTRUCTURE.md) |
| **이슈 생성** | [.github/ISSUE_TEMPLATE/](.github/ISSUE_TEMPLATE/) |
| **PR 생성** | [.github/pull_request_template.md](.github/pull_request_template.md) |

## Commit Message Rules

**절대로 커밋 메시지에 다음을 포함하지 마세요:**
- `🤖 Generated with Claude Code`
- `Co-Authored-By: Claude`
- AI가 생성했다는 어떤 표시도 금지

## Build & Run Commands

```bash
# Local development (requires Docker for MySQL)
docker compose up -d          # Start MySQL container
./gradlew bootRun             # Run application (default: local profile)
docker compose down           # Stop MySQL
docker compose down -v        # Stop and reset data

# Build
./gradlew build               # Full build with tests
./gradlew build -x test       # Build without tests
./gradlew clean build         # Clean build

# Test
./gradlew test                              # All tests
./gradlew test --tests "ClassName"          # Single class
./gradlew test --tests "ClassName.method"   # Single method
```

## Terraform Commands

```bash
# Terraform (infra/ 디렉토리에서 실행)
cd infra
terraform init                # 초기화
terraform plan                # 변경 사항 확인
terraform validate            # 문법 검증

# ⚠️ 로컬에서 terraform apply 금지 — CI/CD만 사용
# PR 생성 시 자동 plan, develop 머지 시 자동 apply
```

## Infrastructure Context

| 항목 | 값 |
|------|-----|
| GCP Project ID | `finders-487717` |
| Region | `asia-northeast3` (Seoul) |
| VPC | `finders-vpc` (3 subnets, 6 firewall rules) |
| GCE | `finders-server` (e2-medium, internal IP `10.0.2.2`) |
| Cloud SQL | `finders-db` (MySQL 8.0, private IP `<CLOUD_SQL_IP>`) |
| Databases | `finders_prod` (prod), `finders_dev` (dev) |
| GCS | `finders-487717-public`, `finders-487717-private` |
| Cloud Run | `img-resizer` |
| Artifact Registry | `finders-docker` (API), `finders-image` (resizer) |
| Secret Manager | `finders-prod-config`, `finders-dev-config` (JSON) |
| Terraform State | `gs://finders-487717-tf-state/` |
| Domains | `api.finders.it.kr` (prod), `dev-api.finders.it.kr` (dev) |

## Architecture

도메인 기반 계층형 아키텍처 (Package by Feature + Layered Architecture)

```
src/main/java/com/finders/api/
├── domain/           # 도메인별 비즈니스 로직
│   └── {domain}/
│       ├── controller/
│       ├── service/
│       │   ├── command/   # CUD 서비스 (interface + impl)
│       │   └── query/     # 조회 서비스 (interface + impl)
│       ├── repository/
│       ├── entity/
│       ├── dto/
│       └── enums/
├── infra/            # 외부 서비스 연동 (OAuth, Google Cloud, Storage)
└── global/           # 공통 모듈 (config, response, exception)
```

**Domains**: member, auth, store, reservation, photo, community, inquiry
