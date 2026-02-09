# 환경 변수 관리 가이드 (Environment Variables)

> Finders API 프로젝트의 환경 변수 관리 전략, 로컬 설정 방법 및 팀 협업 가이드입니다.

---

## 목차
- [개요](#개요)
- [환경 변수 관리 전략 (3계층 시스템)](#환경-변수-관리-전략-3계층-시스템)
- [로컬 개발 환경 설정](#로컬-개발-환경-설정)
- [비밀 정보 접근 방법 (GCP Secret Manager)](#비밀-정보-접근-방법-gcp-secret-manager)
- [팀 협업 가이드](#팀-협업-가이드)
- [변경 이력 (Change Log)](#변경-이력-change-log)
- [보안 가이드라인](#보안-가이드라인)
- [문제 해결 (Troubleshooting)](#문제-해결-troubleshooting)

---

## 개요

Finders 프로젝트는 보안성과 편의성을 동시에 확보하기 위해 환경 변수를 체계적으로 관리합니다. 모든 팀원은 이 가이드를 숙지하여 보안 사고를 예방하고 효율적으로 협업해야 합니다.

상세한 인프라 관점의 비밀 정보 관리는 [SECRET_MANAGEMENT.md](./infra/SECRET_MANAGEMENT.md)를 참고하세요.

---

## 환경 변수 관리 전략 (3계층 시스템)

우리는 다음 세 가지 계층을 통해 환경 변수를 관리합니다.

1. **`.env.example` (Git 추적)**
   - **역할**: 환경 변수의 스키마/템플릿 제공
   - **내용**: 변수명, 설명, 안전한 기본값 (실제 비밀 정보 포함 금지)
   - **목적**: 새로운 팀원이 필요한 변수를 한눈에 파악하고 로컬 설정을 시작할 수 있게 함

2. **`docs/ENV_VARIABLES.md` (Git 추적 - 본 문서)**
   - **역할**: 변경 이력 추적 및 팀 협업 가이드
   - **내용**: 변수 추가/수정/삭제 기록, 관리 정책
   - **목적**: 환경 변수의 변화를 투명하게 기록하고 공유함

3. **GCP Secret Manager (IAM 제어)**
   - **역할**: 실제 비밀 정보(Secrets)의 중앙 저장소
   - **내용**: DB 비밀번호, API 키, JWT 시크릿 등 민감한 값
   - **목적**: 보안 강화, 버전 관리, 접근 권한 제어

---

## 로컬 개발 환경 설정

로컬 개발을 위해 다음 단계를 수행하세요.

1. **템플릿 복사**:
   ```bash
   cp .env.example .env
   ```

2. **`.env` 파일 수정**:
   - `.env` 파일은 Git 추적 대상이 아니므로 자유롭게 수정 가능합니다.
   - 로컬 개발에 필요한 값(예: `SPRING_PROFILES_ACTIVE=local`)을 설정합니다.
   - 민감한 API 키 등이 필요한 경우, 아래 [비밀 정보 접근 방법](#비밀-정보-접근-방법-gcp-secret-manager)을 참고하여 값을 가져와 채워 넣습니다.

3. **애플리케이션 실행**:
   - Spring Boot는 실행 시 로컬의 `.env` 파일을 자동으로 로드하여 환경 변수로 주입합니다.

---

## 비밀 정보 접근 방법 (GCP Secret Manager)

팀원은 권한에 따라 GCP Secret Manager에 저장된 실제 값을 조회할 수 있습니다.

### 사전 준비
- `gcloud` CLI 설치 및 인증 완료 (`gcloud auth application-default login`)
- 프로젝트 접근 권한 확인 (DevOps Lead에게 요청)

### 주요 명령어

#### 1. 비밀 정보 목록 조회
특정 환경(dev/prod)에 해당하는 비밀 정보 목록을 확인합니다.
```bash
# 개발(dev) 환경 비밀 정보 목록
gcloud secrets list --filter="labels.env=dev"

# 운영(prod) 환경 비밀 정보 목록
gcloud secrets list --filter="labels.env=prod"
```

#### 2. 특정 비밀 정보 값 확인
실제 값을 터미널에 출력합니다. (유출 주의)
```bash
# 예: 개발 환경 DB 비밀번호 확인
gcloud secrets versions access latest --secret="app-dev-spring-datasource-password"
```

#### 3. 명명 규칙
- `app-{environment}-{variable-name}` 형식을 따릅니다.
- 예: `app-prod-jwt-secret`, `app-dev-kakao-client-id`

---

## 팀 협업 가이드

### 새로운 환경 변수가 필요한 경우
1. **`.env.example` 업데이트**: 새로운 변수와 설명을 추가하여 PR을 올립니다.
2. **Secret Manager 등록 요청**: 민감한 값인 경우 DevOps Lead(@sachi009955) 또는 Team Lead(@wldy4627)에게 등록을 요청합니다.
3. **변경 이력 기록**: 본 문서의 [변경 이력](#변경-이력-change-log) 섹션에 내용을 추가합니다.

### 환경 변수 값이 변경된 경우
1. **공지**: 팀 채팅방에 변경 사실을 알립니다.
2. **Secret Manager 업데이트**: 권한이 있는 관리자가 새 버전을 등록합니다.
3. **로컬 업데이트**: 팀원들은 각자의 `.env` 파일을 최신 값으로 업데이트합니다.

---

## 변경 이력 (Change Log)

환경 변수의 추가, 수정, 삭제 내역을 기록합니다.

| 날짜 | 변수명 | 변경 내용 | 담당자 |
|------|--------|-----------|--------|
| 2026-02-09 | - | 문서 초기 생성 및 관리 전략 수립 | Sisyphus |
| 2026-01-20 | `REPLICATE_API_KEY` | 사진 복원 기능을 위한 API 키 추가 | sachi009955 |
| 2026-01-15 | `JWT_SECRET` | 보안 강화를 위한 키 순환(Rotation) | wldy4627 |

---

## 보안 가이드라인

1. **절대 금지**: 실제 비밀 정보를 `.env.example`이나 코드, GitHub Issue/PR 코멘트에 직접 노출하지 마세요.
2. **`.gitignore` 확인**: `.env`, `.env.local`, `.env.*.local` 등이 제외되어 있는지 항상 확인하세요.
3. **최소 권한**: 로컬 개발 시에는 가급적 `dev` 환경의 값을 사용하고, `prod` 값 접근은 최소화하세요.
4. **유출 의심 시**: 비밀 정보가 노출되었다고 판단되면 즉시 DevOps Lead에게 보고하여 키 순환(Rotation)을 진행하세요.

---

## 문제 해결 (Troubleshooting)

### Q: `gcloud` 명령 실행 시 "Permission denied"가 발생합니다.
- **A**: `gcloud auth application-default login`으로 다시 인증해 보세요. 여전히 안 된다면 본인의 계정에 `roles/secretmanager.secretAccessor` 권한이 있는지 관리자에게 확인 요청하세요.

### Q: `.env` 파일을 수정했는데 반영이 안 됩니다.
- **A**: 애플리케이션을 재시작해야 합니다. IDE(IntelliJ 등)에서 환경 변수 캐시가 남는 경우 IDE를 재시작하거나 Run Configuration 설정을 확인하세요.

### Q: Secret Manager에 찾는 변수가 없습니다.
- **A**: 명명 규칙(`app-{env}-{name}`)을 다시 확인하거나, 아직 등록되지 않은 변수일 수 있으니 관리자에게 문의하세요.

---

**마지막 업데이트**: 2026-02-09
