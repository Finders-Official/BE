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

## 비밀 정보 명명 규칙

일관된 관리를 위해 다음과 같은 명명 규칙을 준수합니다.

```text
app-{environment}-{variable-name}
```

### 예시
- `app-prod-spring-datasource-password`: 운영 환경 DB 비밀번호
- `app-dev-jwt-secret`: 개발 환경 JWT 서명 키
- `app-prod-oauth2-kakao-client-id`: 운영 환경 카카오 OAuth 클라이언트 ID

---

## 주요 작업 가이드

`gcloud` CLI를 사용한 일반적인 작업 방법입니다.

### 1. 비밀 정보 목록 조회
```bash
# 특정 환경(label)의 비밀 정보 목록 확인
gcloud secrets list --filter="labels.env=prod"
```

### 2. 비밀 정보 값 확인
```bash
# 최신 버전의 값 확인
gcloud secrets versions access latest --secret="app-prod-spring-datasource-password"
```

### 3. 새로운 비밀 정보 생성
```bash
echo -n "my-secret-value" | gcloud secrets create app-prod-new-secret \
  --replication-policy="automatic" \
  --data-file=- \
  --labels="env=prod,managed-by=team"
```

### 4. 기존 비밀 정보 업데이트 (새 버전 추가)
```bash
echo -n "new-value" | gcloud secrets versions add app-prod-existing-secret --data-file=-
```

### 5. 비밀 정보 순환 (Rotation)
1. 새로운 버전의 값을 추가합니다 (위의 업데이트 명령어 사용).
2. 서버를 재시작하여 새로운 값을 적용합니다.
   ```bash
   gcloud compute instances reset finders-server-v2 --zone=asia-northeast3-a
   ```

---

## CI/CD 통합

Finders의 CI/CD 파이프라인은 다음과 같이 Secret Manager와 통합되어 있습니다.

1. **인증**: GitHub Actions는 Workload Identity Federation(WIF)을 통해 GCP에 인증합니다.
2. **이미지 관리**: Docker Hub 대신 GCP Artifact Registry를 사용합니다.
3. **배포**: 서버 시작 스크립트(`startup.sh`)가 실행될 때 Secret Manager에서 필요한 값을 가져와 환경 변수로 설정합니다.
4. **보안**: GitHub Actions 로그에 실제 비밀 정보가 노출되지 않습니다.

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

**마지막 업데이트**: 2026-02-09
