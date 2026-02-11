# Secret Management 가이드 (GCP Secret Manager)

> Finders API 서버의 비밀 정보(Secrets) 관리 및 GCP Secret Manager 워크플로우 가이드

## 목차
- [개요](#개요)
- [IAM 역할 및 권한](#iam-역할-및-권한)
- [비밀 정보 명명 규칙](#비밀-정보-명명-규칙)
- [주요 작업 가이드](#주요-작업-가이드)
- [CI/CD 통합](#cicd-통합)
- [로컬 개발 환경 설정](#로컬-개발-환경-설정)
- [긴급 대응 절차](#긴급-대응-절차)
- [문제 해결 (Troubleshooting)](#문제-해결-troubleshooting)
- [온보딩 체크리스트](#온보딩-체크리스트)

---

## 개요

Finders 프로젝트는 보안 강화와 중앙 집중식 관리를 위해 기존 GitHub Secrets 방식에서 **GCP Secret Manager**로 마이그레이션되었습니다.

### 마이그레이션 배경
- **기존**: 16개 이상의 GitHub Secrets (ENV_PROD, ENV_DEV, DOCKER_PASSWORD 등) 관리의 복잡성
- **변경 후**: 3개의 핵심 GitHub Secrets(WIF 인증용) + GCP Secret Manager 중앙 관리

### 주요 장점
- **중앙 집중화**: 모든 환경의 비밀 정보를 한 곳에서 관리
- **자동 순환(Rotation)**: 비밀번호 및 키의 주기적 자동 변경 지원
- **감사 로그**: 누가, 언제, 어떤 비밀 정보에 접근했는지 기록
- **세밀한 권한 제어**: 팀원별/서비스별 접근 권한 분리
- **중복 제거**: 환경별로 중복되던 설정값들을 효율적으로 관리

---

## IAM 역할 및 권한

사용자 유형에 따라 다음과 같은 권한이 부여됩니다.

| 역할 | 대상 | 주요 권한 |
|------|------|-----------|
| **DevOps Lead** | sachi009955@gmail.com | Secret Manager 관리자, IAM 보안 관리자, 프로젝트 관리자 |
| **Team Lead** | wldy4627@gmail.com | Secret Manager 관리자, Compute Engine 관리자 |
| **Team Members** | 팀원 8명 | Secret Manager 접근자 (Read-only), 모니터링 뷰어 |

### 상세 권한 (Secret Manager 관련)
- `roles/secretmanager.admin`: 비밀 정보 생성, 수정, 삭제, 권한 설정 등 모든 권한
- `roles/secretmanager.secretAccessor`: 비밀 정보의 값을 읽을 수 있는 권한 (애플리케이션 및 일반 팀원)

---

## 시크릿 구조

환경별로 **JSON 통합 시크릿**을 사용합니다. 개별 시크릿이 아니라, 환경별 설정값을 하나의 JSON으로 묶어 관리합니다.

### Secret Manager 시크릿 목록

| 시크릿 이름 | 형식 | 용도 |
|-------------|------|------|
| `finders-prod-config` | JSON | Prod 환경 전체 설정 (DB, JWT, OAuth, Redis 등) |
| `finders-dev-config` | JSON | Dev 환경 전체 설정 |

### JSON 구조 예시

```json
{
  "SPRING_DATASOURCE_URL": "jdbc:mysql://10.68.240.3:3306/finders",
  "SPRING_DATASOURCE_USERNAME": "finders",
  "SPRING_DATASOURCE_PASSWORD": "...",
  "JWT_SECRET": "...",
  "OAUTH2_KAKAO_CLIENT_ID": "...",
  "REDIS_HOST": "...",
  "REDIS_PASSWORD": "...",
  "GCS_BUCKET_PUBLIC": "finders-public",
  "GCS_BUCKET_PRIVATE": "finders-private"
}
```

### 배포 시 사용 흐름

1. GitHub Actions가 WIF로 GCP 인증
2. `gcloud secrets versions access latest --secret="finders-{env}-config"` 로 JSON 가져옴
3. JSON의 각 키-값을 Docker 환경변수로 주입
4. Spring Boot가 환경변수에서 설정값 읽음

---

## 주요 작업 가이드

`gcloud` CLI를 사용한 일반적인 작업 방법입니다.

### 1. 시크릿 목록 조회
```bash
gcloud secrets list
```

### 2. 현재 설정값 확인
```bash
# Prod 설정 전체 확인
gcloud secrets versions access latest --secret="finders-prod-config"

# Dev 설정 전체 확인
gcloud secrets versions access latest --secret="finders-dev-config"

# JSON에서 특정 키만 추출
gcloud secrets versions access latest --secret="finders-prod-config" | jq '.JWT_SECRET'
```

### 3. 설정값 업데이트
```bash
# 1. 현재 값을 파일로 내려받기
gcloud secrets versions access latest --secret="finders-prod-config" > /tmp/config.json

# 2. 파일 수정 (JSON 편집)
vim /tmp/config.json

# 3. 새 버전으로 업로드
gcloud secrets versions add finders-prod-config --data-file=/tmp/config.json

# 4. 임시 파일 삭제
rm /tmp/config.json
```

### 4. 설정 변경 후 적용
시크릿 변경 후 서버 재배포가 필요합니다. CD 파이프라인이 자동으로 최신 시크릿을 가져오므로, **해당 브랜치에 빈 커밋을 push하거나 GitHub Actions에서 수동 실행**하면 됩니다.

---

## CI/CD 통합

### GitHub Secrets (최소한의 3개만)

| Secret | 용도 |
|--------|------|
| `WIF_PROVIDER` | WIF Provider 리소스 경로 |
| `WIF_SERVICE_ACCOUNT` | `terraform-ci` SA 이메일 |
| `GCP_PROJECT_ID` | GCP 프로젝트 ID |

이 3개로 GCP에 인증한 후, 앱 설정값은 전부 Secret Manager에서 가져옵니다.

### 인증 흐름

```
GitHub Actions → WIF (finders-pool/github-provider) → terraform-ci SA → Secret Manager
```

- Docker Hub 대신 GCP Artifact Registry 사용 (WIF로 인증)
- 앱 설정값은 배포 스크립트가 Secret Manager에서 가져와 Docker 환경변수로 주입
- GitHub Actions 로그에 비밀 정보가 노출되지 않음

---

## 로컬 개발 환경 설정

로컬에서 Secret Manager의 값을 테스트하거나 사용해야 하는 경우:

1. **gcloud 인증**:
   ```bash
   gcloud auth application-default login
   ```
2. **권한 확인**: 본인의 계정에 `roles/secretmanager.secretAccessor` 권한이 있는지 확인합니다.
3. **값 가져오기**: 위의 [주요 작업 가이드](#2-비밀-정보-값-확인)를 참고하여 값을 확인합니다.

> ⚠️ **주의**: 로컬 `.env` 파일에 실제 운영 환경의 비밀 정보를 저장하고 커밋하지 않도록 각별히 주의하세요.

---

## 긴급 대응 절차

비밀 정보가 유출되었거나 의심되는 경우 다음 절차를 따릅니다.

1. **즉시 순환**: 해당 비밀 정보의 새 버전을 생성하여 값을 변경합니다.
2. **서비스 재시작**: 영향을 받는 모든 서비스를 재시작하여 새 값을 적용합니다.
3. **로그 조사**: 감사 로그를 확인하여 부정 접근 여부를 조사합니다.
   ```bash
   gcloud logging read "resource.type=secret_manager"
   ```
4. **보고**: DevOps Lead 또는 Team Lead에게 즉시 상황을 공유합니다.

---

## 문제 해결 (Troubleshooting)

### "Permission denied" 에러 발생 시
- 본인의 계정에 적절한 IAM 역할이 부여되었는지 확인합니다.
  ```bash
  gcloud projects get-iam-policy [PROJECT_ID] \
    --flatten="bindings[].members" \
    --filter="bindings.members:user:[YOUR_EMAIL]"
  ```
- Secret Manager API가 활성화되어 있는지 확인합니다.

### 서버에서 변경된 값이 반영되지 않을 때
- Secret Manager의 값은 서버 부팅 시에만 가져옵니다.
- 서버를 재시작(Reset)하여 새로운 값을 다시 로드해야 합니다.

---

## 온보딩 체크리스트

새로운 팀원이 합류하면 다음 사항을 완료해야 합니다.

- [ ] GCP 프로젝트에 사용자 계정 추가 및 IAM 역할 부여 확인
- [ ] 로컬 환경에 `gcloud` CLI 설치 및 인증 완료
- [ ] 비밀 정보 목록 조회 가능 여부 확인 (`gcloud secrets list`)
- [ ] 본 가이드 문서 숙지
- [ ] 비밀 정보 명명 규칙 이해
- [ ] 문제 발생 시 비상 연락망 확인 (DevOps Lead)

---

**마지막 업데이트**: 2026-02-11
