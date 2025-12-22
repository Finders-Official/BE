# Finders API

> 뷰파인더 너머, 취향을 찾다 - 필름 현상소 연결 플랫폼

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Build Tool | Gradle 8.11 |
| Database | MySQL 8.x, H2 (local) |
| ORM | Spring Data JPA, QueryDSL 5.1 |
| Security | Spring Security, JWT |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Cloud | Google Cloud Vision AI |

## Getting Started

### Prerequisites

- Java 17+
- Gradle 8.x
- MySQL 8.x (optional, H2 for local)

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
| `local` | 로컬 개발 환경 | H2 In-Memory |
| `dev` | 개발 서버 | MySQL |
| `prod` | 운영 서버 | MySQL |

```bash
# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

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
