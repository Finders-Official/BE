# Finders Documentation

> 프로젝트 문서 네비게이션

---

## 📐 Architecture

시스템 구조 및 데이터베이스 설계 문서입니다.

| 문서 | 설명 |
|------|------|
| [ARCHITECTURE.md](architecture/ARCHITECTURE.md) | 시스템 아키텍처, 레이어 구조, 디자인 패턴 |
| [ERD.md](architecture/ERD.md) | 데이터베이스 설계서, DDL, Enum 정의 |
| [INFRASTRUCTURE.md](architecture/INFRASTRUCTURE.md) | GCP 인프라 구성, 환경별 설정, 비용 |

## 💻 Development

코드 작성 및 협업 규칙 문서입니다.

| 문서 | 설명 |
|------|------|
| [CODE_STYLE.md](development/CODE_STYLE.md) | Java/Spring Boot 코드 스타일 가이드 |
| [CONVENTIONS.md](development/CONVENTIONS.md) | 네이밍 컨벤션, Git 브랜치/커밋 규칙, 릴리스 프로세스 |
| [API.md](development/API.md) | API 명세, 응답 형식, 에러 코드 |

## 📖 Guides

개발 환경 설정 및 기능별 가이드 문서입니다.

| 문서 | 설명 |
|------|------|
| [GCS_SETUP.md](guides/GCS_SETUP.md) | Google Cloud Storage 설정 가이드 |
| [LOCAL_DEVELOPMENT.md](guides/LOCAL_DEVELOPMENT.md) | 로컬 개발 환경 설정 (GCS Presigned URL 테스트) |
| [ENV_VARIABLES.md](guides/ENV_VARIABLES.md) | 환경 변수 관리 가이드 (로컬 설정, Secret Manager 접근) |
| [REMOTE_DB_ACCESS.md](guides/REMOTE_DB_ACCESS.md) | Cloud SQL 원격 접속 가이드 (IAP 터널) |
| [AUTHENTICATION_PRINCIPAL.md](guides/AUTHENTICATION_PRINCIPAL.md) | 커스텀 인증 객체 (AuthUser) 사용법 |

## 🌐 Infrastructure

인프라 운영 문서입니다. 상세 인덱스는 [infra/README.md](infra/README.md)를 참고하세요.

| 문서 | 설명 | 대상 |
|------|------|------|
| [SECRET_MANAGEMENT.md](infra/SECRET_MANAGEMENT.md) | GCP Secret Manager 관리 가이드 | 모든 개발자 |
| [GCP_LOGGING_GUIDE.md](infra/GCP_LOGGING_GUIDE.md) | GCP Cloud Logging 확인 가이드 | DevOps |
| [IAC_TERRAFORM_INTRO.md](infra/IAC_TERRAFORM_INTRO.md) | IaC/Terraform 개념 학습 | 모든 개발자 |
| [TERRAFORM_OPERATIONS.md](infra/TERRAFORM_OPERATIONS.md) | Terraform 운영 가이드 | DevOps |

## 🤝 Contributing

- [CONTRIBUTING.md](../CONTRIBUTING.md) - 기여 가이드
- [SECURITY.md](../SECURITY.md) - 보안 정책

---

**마지막 업데이트**: 2026-02-11
