<!-- Banner -->
<!-- <p align="center">
  <img src="docs/images/banner.png" alt="Finders Banner" width="100%">
</p> -->

<h1 align="center">Finders API</h1>

<p align="center">
  <strong>뷰파인더 너머, 취향을 찾다</strong><br>
  필름 현상소 연결 플랫폼 백엔드
</p>

<p align="center">
  <a href="https://github.com/Finders-Official/BE/actions/workflows/ci.yml">
    <img src="https://github.com/Finders-Official/BE/actions/workflows/ci.yml/badge.svg" alt="CI">
  </a>
  <a href="https://github.com/Finders-Official/BE/actions/workflows/deploy-dev.yml">
    <img src="https://github.com/Finders-Official/BE/actions/workflows/deploy-dev.yml/badge.svg" alt="Deploy Dev">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License">
  </a>
</p>

<br>

## About

Finders는 필름 사진 애호가와 현상소를 연결하는 플랫폼입니다. 현상소 검색 및 예약, 필름 현상/인화 주문 관리, AI 사진 복원, 커뮤니티 기능을 제공합니다.

<br>

## Tech Stack

<table>
  <tr>
    <th>Category</th>
    <th>Technology</th>
  </tr>
  <tr>
    <td><b>Language</b></td>
    <td>
      <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
    </td>
  </tr>
  <tr>
    <td><b>Framework</b></td>
    <td>
      <img src="https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3.4">
      <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white" alt="Spring Security">
    </td>
  </tr>
  <tr>
    <td><b>Database</b></td>
    <td>
      <img src="https://img.shields.io/badge/MySQL_8-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL 8">
      <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis">
    </td>
  </tr>
  <tr>
    <td><b>ORM</b></td>
    <td>
      <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA">
      <img src="https://img.shields.io/badge/QueryDSL_5.1-0769AD?style=for-the-badge" alt="QueryDSL 5.1">
    </td>
  </tr>
  <tr>
    <td><b>Cloud</b></td>
    <td>
      <img src="https://img.shields.io/badge/Google_Cloud-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white" alt="GCP">
      <img src="https://img.shields.io/badge/Terraform-7B42BC?style=for-the-badge&logo=terraform&logoColor=white" alt="Terraform">
    </td>
  </tr>
  <tr>
    <td><b>CI/CD</b></td>
    <td>
      <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white" alt="GitHub Actions">
      <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
    </td>
  </tr>
  <tr>
    <td><b>Infra</b></td>
    <td>
      <img src="https://img.shields.io/badge/Traefik-24A1C1?style=for-the-badge&logo=traefik-proxy&logoColor=white" alt="Traefik">
      <img src="https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=cloudflare&logoColor=white" alt="Cloudflare">
    </td>
  </tr>
  <tr>
    <td><b>API Docs</b></td>
    <td>
      <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger">
    </td>
  </tr>
  <tr>
    <td><b>AI</b></td>
    <td>
      <img src="https://img.shields.io/badge/Replicate_(SUPIR)-000000?style=for-the-badge" alt="Replicate SUPIR">
    </td>
  </tr>
</table>

<br>

## Architecture

<!-- TODO: Replace with actual architecture diagram -->
<!-- <p align="center">
  <img src="docs/images/architecture.png" alt="Architecture" width="90%">
</p> -->

| Component | Detail |
|-----------|--------|
| **Reverse Proxy** | Traefik (Host-based routing, Blue-Green deploy) |
| **Tunnel** | Cloudflare Tunnel (SSL termination, DDoS protection) |
| **Compute** | GCE e2-medium (Docker Compose) |
| **Database** | Cloud SQL for MySQL 8.0 (Private IP) |
| **Storage** | Google Cloud Storage (Public/Private buckets) |
| **Image Resizer** | Cloud Run (`img-resizer`) |
| **Registry** | Artifact Registry |
| **Secrets** | GCP Secret Manager |
| **IaC** | Terraform (auto plan on PR, auto apply on merge) |

### Environment

| Env | Branch | Domain | Deploy |
|-----|--------|--------|--------|
| **Dev** | `develop` | `dev-api.finders.it.kr` | Blue-Green (auto) |
| **Prod** | `main` | `api.finders.it.kr` | Blue-Green (auto) |

<br>

## ERD

<!-- TODO: Replace with actual ERD diagram -->
<!-- <p align="center">
  <img src="docs/images/erd.png" alt="ERD" width="90%">
</p> -->

38 tables across 7 domains. Full schema: [docs/architecture/ERD.md](docs/architecture/ERD.md)

| Domain | Tables | Description |
|--------|--------|-------------|
| **Member** | 12 | 회원, 소셜 계정, 배송지, 크레딧, 약관, 디바이스 |
| **Store** | 8 | 현상소, 이미지, 태그, 공지, 영업시간, 지역 |
| **Reservation** | 2 | 예약, 예약 슬롯 |
| **Photo** | 7 | 현상 주문, 스캔 사진, 인화 주문, AI 복원, 배송 |
| **Community** | 5 | 게시글, 이미지, 댓글, 좋아요, 검색 이력 |
| **Inquiry** | 3 | 1:1 문의, 첨부 이미지, 답변 |
| **Payment** | 1 | 결제 (PortOne V2) |

<br>

## Project Structure

```
src/main/java/com/finders/api/
├── domain/                     # Feature-based business logic
│   ├── member/                 # 회원 (가입, 프로필, 크레딧)
│   ├── auth/                   # 인증/인가 (OAuth, JWT)
│   ├── store/                  # 현상소 (검색, 상세, 즐겨찾기)
│   ├── reservation/            # 예약 (슬롯, 시간대)
│   ├── photo/                  # 사진 (현상, 인화, AI 복원)
│   ├── community/              # 커뮤니티 (피드, 댓글, 좋아요)
│   └── inquiry/                # 1:1 문의
│
├── infra/                      # External service integration
│   ├── oauth/                  # Kakao, Apple OAuth
│   ├── replicate/              # AI Image Restoration (SUPIR)
│   ├── payment/                # PortOne V2
│   └── storage/                # Google Cloud Storage
│
└── global/                     # Cross-cutting concerns
    ├── config/                 # Configuration
    ├── security/               # JWT, Spring Security
    ├── response/               # Unified API response
    └── exception/              # Global exception handling
```

<br>

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 8.x

### Quick Start

```bash
# Clone & setup
git clone https://github.com/Finders-Official/BE.git
cd BE
cp .env.example .env

# Start database
docker compose up -d

# Run application
./gradlew bootRun

# Verify
open http://localhost:8080/api/swagger-ui.html
```

### Build & Test

```bash
./gradlew build               # Full build with tests
./gradlew build -x test       # Build without tests
./gradlew test                # Run all tests
```

<br>

## API Documentation

| Env | Swagger UI | OpenAPI Spec |
|-----|------------|--------------|
| Local | `localhost:8080/api/swagger-ui.html` | `localhost:8080/api/v3/api-docs` |
| Dev | `dev-api.finders.it.kr/api/swagger-ui.html` | `dev-api.finders.it.kr/api/v3/api-docs` |

Full API specification: [docs/development/API.md](docs/development/API.md)

<br>

## Documentation

| Category | Document | Description |
|----------|----------|-------------|
| **Architecture** | [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) | System architecture, layer structure |
| | [ERD.md](docs/architecture/ERD.md) | Database schema, DDL, enums |
| | [INFRASTRUCTURE.md](docs/architecture/INFRASTRUCTURE.md) | GCP infra, environments, cost |
| **Development** | [CODE_STYLE.md](docs/development/CODE_STYLE.md) | Java/Spring Boot code style |
| | [CONVENTIONS.md](docs/development/CONVENTIONS.md) | Naming, Git branch/commit rules |
| | [API.md](docs/development/API.md) | API spec, response format, error codes |
| **Guides** | [LOCAL_DEVELOPMENT.md](docs/guides/LOCAL_DEVELOPMENT.md) | Local dev environment setup |
| | [ENV_VARIABLES.md](docs/guides/ENV_VARIABLES.md) | Environment variable management |
| | [REMOTE_DB_ACCESS.md](docs/guides/REMOTE_DB_ACCESS.md) | Cloud SQL remote access (IAP) |
| **Infrastructure** | [TERRAFORM_OPERATIONS.md](docs/infra/TERRAFORM_OPERATIONS.md) | Terraform operations guide |
| | [SECRET_MANAGEMENT.md](docs/infra/SECRET_MANAGEMENT.md) | GCP Secret Manager guide |

<br>

## Team

<!-- TODO: Replace placeholder with actual team member info and photos -->
<!-- Store profile images at: docs/images/members/{name}.png -->

<table>
  <tr>
    <td align="center" width="150">
      <!-- <img src="docs/images/members/member1.png" width="100" alt="Member 1"><br> -->
      <b>Member 1</b><br>
      <sub>BE / Leader</sub><br>
      <a href="https://github.com/">GitHub</a>
    </td>
    <td align="center" width="150">
      <!-- <img src="docs/images/members/member2.png" width="100" alt="Member 2"><br> -->
      <b>Member 2</b><br>
      <sub>BE</sub><br>
      <a href="https://github.com/">GitHub</a>
    </td>
    <td align="center" width="150">
      <!-- <img src="docs/images/members/member3.png" width="100" alt="Member 3"><br> -->
      <b>Member 3</b><br>
      <sub>BE</sub><br>
      <a href="https://github.com/">GitHub</a>
    </td>
  </tr>
</table>

<br>

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## Security

See [SECURITY.md](SECURITY.md) for our security policy and vulnerability reporting.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
