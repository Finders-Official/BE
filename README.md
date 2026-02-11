# Finders API

[![CI](https://github.com/Finders-Official/BE/actions/workflows/ci.yml/badge.svg)](https://github.com/Finders-Official/BE/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> 뷰파인더 너머, 취향을 찾다 - 필름 현상소 연결 플랫폼

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.11 |
| Build Tool | Gradle 8.11 |
| Database | MySQL 8.x, Redis |
| ORM | Spring Data JPA, QueryDSL 5.1 |
| Security | Spring Security, JWT |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Cloud | GCP (Compute Engine, Cloud SQL, Cloud Storage, Cloud Run, Artifact Registry) |
| IaC | Terraform |
| CI/CD | GitHub Actions (Blue-Green Deploy) |
| Reverse Proxy | Traefik, Cloudflare Tunnel |

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 8.x

### Installation

```bash
# 1. Clone repository
git clone https://github.com/Finders-Official/BE.git
cd BE

# 2. Copy environment file
cp .env.example .env
# Edit .env with your configuration

# 3. Build project
./gradlew build

# 4. Run application
./gradlew bootRun
```

### Profiles

| Profile | Description | Database | Redis |
|---------|-------------|----------|-------|
| `local` | 로컬 개발 환경 | MySQL (Docker) | Docker Redis |
| `dev` | 개발 서버 | GCP Cloud SQL (`finders_dev`) | Docker Redis (GCE 내) |
| `prod` | 운영 서버 | GCP Cloud SQL (`finders`) | Upstash Redis |

### 로컬 개발 (권장)

```bash
# 1. MySQL 컨테이너 실행
docker compose up -d

# 2. Spring Boot 실행 (기본 profile: local)
./gradlew bootRun

# 3. Swagger 확인
open http://localhost:8080/api/swagger-ui.html

# 4. 종료
docker compose down

# 데이터 초기화
docker compose down -v
```

| 서비스 | 접속 정보 |
|--------|-----------|
| MySQL | `localhost:3306` |
| DB Name | `finders` |
| Username | `finders` |
| Password | `finders123` |

## API Documentation

애플리케이션 실행 후 아래 URL에서 확인:

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/api/v3/api-docs

## Project Structure

```
src/main/java/com/finders/api/
├── domain/                     # 도메인별 비즈니스 로직
│   ├── member/                 # 회원
│   ├── auth/                   # 인증/인가
│   ├── store/                  # 현상소
│   ├── reservation/            # 예약
│   ├── photo/                  # 사진
│   ├── community/              # 커뮤니티
│   └── inquiry/                # 1:1 문의
│
├── infra/                      # 외부 서비스 연동
│   ├── oauth/                  # OAuth (Kakao, Apple)
│   ├── google/                 # Google Cloud (Vision AI)
│   └── storage/                # 파일 저장소
│
└── global/                     # 공통 모듈
    ├── config/                 # 설정
    ├── entity/                 # Base Entity
    ├── response/               # API 응답 구조
    └── exception/              # 예외 처리
```

## Development

### Build

```bash
./gradlew build          # 빌드
./gradlew clean build    # 클린 빌드
./gradlew build -x test  # 테스트 제외 빌드
```

### Test

```bash
./gradlew test                              # 전체 테스트
./gradlew test --tests "ClassName"          # 특정 클래스 테스트
./gradlew test --tests "ClassName.method"   # 특정 메서드 테스트
```

### Code Style

- 상세 코드 스타일: [docs/development/CODE_STYLE.md](docs/development/CODE_STYLE.md)
- 네이밍/Git 컨벤션: [docs/development/CONVENTIONS.md](docs/development/CONVENTIONS.md)

| 핵심 기술 | 설명 |
|----------|------|
| **record DTO** | 불변성 보장, 보일러플레이트 제거 |
| **Virtual Threads** | 동시성 처리 성능 향상 |
| **FixtureMonkey** | 테스트 데이터 자동 생성 |

## Documentation

전체 문서 목록은 [docs/README.md](docs/README.md)를 참고하세요.

### Architecture
- [Architecture](docs/architecture/ARCHITECTURE.md) - 시스템 아키텍처
- [ERD](docs/architecture/ERD.md) - 데이터베이스 설계서
- [Infrastructure](docs/architecture/INFRASTRUCTURE.md) - 인프라 구성

### Development
- [Code Style](docs/development/CODE_STYLE.md) - 코드 스타일 가이드
- [Conventions](docs/development/CONVENTIONS.md) - 네이밍/Git 컨벤션
- [API](docs/development/API.md) - API 명세

### Guides
- [GCS Setup](docs/guides/GCS_SETUP.md) - GCS 설정 가이드
- [Local Development](docs/guides/LOCAL_DEVELOPMENT.md) - 로컬 개발 환경 설정
- [Remote DB Access](docs/guides/REMOTE_DB_ACCESS.md) - 원격 DB 접속 가이드
- [AuthenticationPrincipal](docs/guides/AUTHENTICATION_PRINCIPAL.md) - 인증 객체 사용법

### Infrastructure
- [Infrastructure Docs](docs/infra/README.md) - 인프라 문서 인덱스
- [Terraform Operations](docs/infra/TERRAFORM_OPERATIONS.md) - Terraform 운영 가이드

## Contributing

기여 방법은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.

## License

이 프로젝트는 [Apache License 2.0](LICENSE) 하에 배포됩니다.
