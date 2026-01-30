# Contributing Guide

Finders API 프로젝트에 기여해주셔서 감사합니다.

---

## 개발 환경

### 필수 요구사항

- Java 21+
- Docker & Docker Compose
- Gradle 8.x

### 로컬 환경 설정

```bash
# 1. 저장소 클론
git clone https://github.com/Finders-Official/Spring.git
cd Spring

# 2. 환경 변수 설정
cp .env.example .env
# .env 파일을 수정하세요

# 3. MySQL 컨테이너 실행
docker compose up -d

# 4. 애플리케이션 실행
./gradlew bootRun

# 5. Swagger 확인
open http://localhost:8080/api/swagger-ui.html
```

---

## 기여 프로세스

### 1. 이슈 생성

작업 시작 전 반드시 GitHub Issue를 먼저 생성합니다.

- [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) - 버그 발견 시
- [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) - 새 기능 제안 시
- [Task](.github/ISSUE_TEMPLATE/task.md) - 일반 작업 시

### 2. 브랜치 생성

이슈 번호를 포함하여 브랜치를 생성합니다.

```
<type>/<설명>-#<issue_number>
```

| Type | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 |
| `hotfix` | 긴급 버그 수정 |
| `chore` | 빌드/설정 변경 |
| `docs` | 문서 수정 |
| `test` | 테스트 코드 |
| `task` | 일반 작업 |

예시:
```bash
git checkout develop
git pull origin develop
git checkout -b feat/signup-api-#14
```

### 3. 개발

- 코드 스타일: [docs/development/CODE_STYLE.md](docs/development/CODE_STYLE.md)
- 네이밍/Git 컨벤션: [docs/development/CONVENTIONS.md](docs/development/CONVENTIONS.md)

### 4. 커밋

```
<type>: <subject> (#<issue_number>)

<body>
```

- **type**: 소문자 영문 (`feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `rename`, `remove`)
- **subject**: 한글 또는 영문, 50자 이내, 마침표 없음
- **body**: 한글 작성 권장, 무엇을 왜 변경했는지 설명

예시:
```bash
git commit -m "feat: 회원가입 API 구현 (#14)"
```

### 5. PR 제출

`develop` 브랜치로 Pull Request를 생성합니다.

PR 제목 형식:
```
[TYPE] 설명 (#이슈번호)
```

예시: `[FEAT] 회원가입 API 구현 (#14)`

---

## PR 체크리스트

PR 제출 전 아래 항목을 확인하세요.

- [ ] 코드 컨벤션을 준수했습니다 (`docs/development/CONVENTIONS.md` 참고)
- [ ] 로컬에서 빌드가 정상적으로 완료됩니다 (`./gradlew build`)
- [ ] 새로운 기능에 대한 테스트를 작성했습니다 (해당 시)
- [ ] API 변경이 있다면 Swagger 문서가 업데이트되었습니다

---

## 빌드 및 테스트

```bash
# 전체 빌드
./gradlew build

# 테스트만 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "ClassName"
./gradlew test --tests "ClassName.method"

# 테스트 제외 빌드
./gradlew build -x test
```

---

## 참고 문서

- [Code Style Guide](docs/development/CODE_STYLE.md)
- [Conventions](docs/development/CONVENTIONS.md)
- [Architecture](docs/architecture/ARCHITECTURE.md)
- [API Specification](docs/development/API.md)
