# Contributing Guide

Finders API 프로젝트에 기여해주셔서 감사합니다. 이 문서는 원활한 협업을 위한 개발 프로세스를 설명합니다.

<br>

## Development Environment

### Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 21+ |
| Docker & Docker Compose | Latest |
| Gradle | 8.x |

### Local Setup

```bash
git clone https://github.com/Finders-Official/BE.git
cd BE
cp .env.example .env          # Edit with your configuration
docker compose up -d           # Start MySQL
./gradlew bootRun              # Run application
```

Swagger UI: `http://localhost:8080/api/swagger-ui.html`

<br>

## Contribution Workflow

### 1. Create Issue

작업 시작 전 반드시 GitHub Issue를 생성합니다.

| Template | When |
|----------|------|
| [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) | 버그 발견 |
| [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) | 새 기능 제안 |
| [Task](.github/ISSUE_TEMPLATE/task.md) | 일반 작업/개선 |

### 2. Create Branch

`develop` 브랜치에서 분기하며, 이슈 번호를 포함합니다.

```
<type>/<설명>-#<issue_number>
```

| Type | Description | Example |
|------|-------------|---------|
| `feat` | 새로운 기능 | `feat/signup-api-#14` |
| `fix` | 버그 수정 | `fix/image-upload-#23` |
| `refactor` | 코드 리팩토링 | `refactor/token-logic-#8` |
| `hotfix` | 긴급 수정 | `hotfix/null-pointer-#31` |
| `docs` | 문서 수정 | `docs/update-api-docs-#50` |
| `chore` | 빌드/설정 | `chore/ci-pipeline-#3` |
| `test` | 테스트 코드 | `test/auth-unit-tests-#27` |

```bash
git checkout develop
git pull origin develop
git checkout -b feat/signup-api-#14
```

### 3. Development

코드 작성 시 아래 가이드를 참고합니다.

| Guide | Description |
|-------|-------------|
| [CODE_STYLE.md](docs/development/CODE_STYLE.md) | Java/Spring Boot 코드 스타일 |
| [CONVENTIONS.md](docs/development/CONVENTIONS.md) | 네이밍, Git 컨벤션 |

### 4. Commit

```
<type>: <subject> (#<issue_number>)
```

- **type**: 소문자 영문 (`feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `rename`, `remove`)
- **subject**: 한글 또는 영문, 50자 이내, 마침표 없음
- **body** (optional): 무엇을 왜 변경했는지 설명

```bash
git commit -m "feat: 회원가입 API 구현 (#14)"
```

### 5. Pull Request

`develop` 브랜치로 PR을 생성합니다.

**PR 제목 형식:**
```
[TYPE] 설명 (#이슈번호)
```

**Examples:**
- `[FEAT] 회원가입 API 구현 (#14)`
- `[FIX] 이미지 업로드 시 NPE 수정 (#23)`
- `[DOCS] ERD 스키마 업데이트 (#6)`

<br>

## PR Checklist

PR 제출 전 아래 항목을 확인합니다.

- [ ] 코드 컨벤션 준수 ([CONVENTIONS.md](docs/development/CONVENTIONS.md))
- [ ] 로컬 빌드 성공 (`./gradlew build`)
- [ ] 새 기능에 대한 테스트 작성 (해당 시)
- [ ] API 변경 시 Swagger 문서 업데이트

<br>

## Build & Test

```bash
./gradlew build                             # Full build with tests
./gradlew build -x test                     # Build without tests
./gradlew test                              # All tests
./gradlew test --tests "ClassName"          # Single class
./gradlew test --tests "ClassName.method"   # Single method
```

<br>

## Infrastructure (Terraform)

인프라 변경은 `infra/` 디렉토리의 Terraform 코드를 수정합니다.

```bash
cd infra
terraform init        # Initialize
terraform validate    # Syntax check
terraform plan        # Preview changes
```

**Important:**
- 로컬에서 `terraform apply` **절대 금지** (CI/CD 전용)
- PR 생성 시 자동 `terraform plan` 실행 (PR 코멘트로 결과 표시)
- `develop` 머지 시 자동 `terraform apply` 실행
- `terraform.tfvars` 커밋 금지 (`.gitignore` 처리됨)

상세 가이드: [TERRAFORM_OPERATIONS.md](docs/infra/TERRAFORM_OPERATIONS.md)

<br>

## References

| Document | Description |
|----------|-------------|
| [CODE_STYLE.md](docs/development/CODE_STYLE.md) | Code style guide |
| [CONVENTIONS.md](docs/development/CONVENTIONS.md) | Naming and Git conventions |
| [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) | System architecture |
| [INFRASTRUCTURE.md](docs/architecture/INFRASTRUCTURE.md) | Infrastructure design |
| [API.md](docs/development/API.md) | API specification |
| [TERRAFORM_OPERATIONS.md](docs/infra/TERRAFORM_OPERATIONS.md) | Terraform operations |
