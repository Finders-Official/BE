# Security Policy

<br>

## Supported Versions

현재 최신 릴리스 버전에 대해서만 보안 패치를 지원합니다.

| Version | Supported |
|---------|-----------|
| Latest release | Yes |
| Previous releases | No |

<br>

## Reporting a Vulnerability

보안 취약점을 발견한 경우, **공개 이슈로 등록하지 마세요.** 아래 방법을 이용해주세요.

### Preferred: GitHub Security Advisory

[Security Advisory 작성](https://github.com/Finders-Official/BE/security/advisories/new)

### Alternative: Direct Contact

프로젝트 관리자에게 비공개로 연락합니다.

### Required Information

| Item | Description |
|------|-------------|
| **Type** | 취약점 유형 (XSS, SQLi, Auth bypass 등) |
| **Description** | 상세 설명 |
| **Steps to Reproduce** | 재현 절차 |
| **Impact** | 영향 범위 및 심각도 |
| **Fix Suggestion** | 수정 방안 (optional) |

<br>

## Response Process

| Phase | Timeline | Description |
|-------|----------|-------------|
| **Acknowledgment** | 3 business days | 신고 접수 확인 |
| **Assessment** | 1 week | 심각도 및 영향 범위 평가 |
| **Patch** | Depends on severity | 패치 개발 및 테스트 |
| **Disclosure** | After fix deployed | 수정 배포 후 취약점 정보 공개 |

<br>

## Secret Management

저장소에 다음 정보를 절대 포함하지 마세요.

| Prohibited | Example |
|-----------|---------|
| API Keys | `REPLICATE_API_TOKEN`, `PORTONE_API_SECRET` |
| Database Credentials | DB password, connection strings |
| Authentication Tokens | JWT secret, refresh tokens |
| Service Account Keys | GCP service account JSON |
| Environment Files | `.env` contents |

### How We Manage Secrets

모든 민감 정보는 **GCP Secret Manager**를 통해 중앙 관리합니다.

| Resource | Purpose |
|----------|---------|
| `finders-prod-config` | Production configuration (JSON) |
| `finders-dev-config` | Development configuration (JSON) |
| GitHub Secrets (3 only) | `WIF_PROVIDER`, `WIF_SERVICE_ACCOUNT`, `GCP_PROJECT_ID` |

상세 가이드: [SECRET_MANAGEMENT.md](docs/infra/SECRET_MANAGEMENT.md)
