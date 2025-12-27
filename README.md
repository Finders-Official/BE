# Finders API

> 뷰파인더 너머, 취향을 찾다 - 필름 현상소 연결 플랫폼

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.13 |
| Build Tool | Gradle 8.11 |
| Database | MySQL 8.x |
| ORM | Spring Data JPA, QueryDSL 5.1 |
| Security | Spring Security, JWT |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Cloud | GCP (Compute Engine, Cloud SQL, Cloud Storage, Vision AI) |

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 8.x

### Installation

```bash
# 1. Clone repository
git clone <repository-url>
cd finders-api

# 2. Copy environment file
cp .env.example .env
# Edit .env with your configuration

# 3. Build project
./gradlew build

# 4. Run application
./gradlew bootRun
```

### Profiles

| Profile | Description | Database |
|---------|-------------|----------|
| `local` | 로컬 개발 환경 | MySQL (Docker) |
| `prod` | 운영 서버 | GCP Cloud SQL |

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

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) 기반
- 상세 컨벤션은 [docs/CONVENTIONS.md](docs/CONVENTIONS.md) 참고

## Documentation

- [Architecture](docs/ARCHITECTURE.md) - 아키텍처 설계
- [Conventions](docs/CONVENTIONS.md) - 코드 컨벤션
- [API Spec](docs/API.md) - API 명세

## License

Private - All rights reserved
