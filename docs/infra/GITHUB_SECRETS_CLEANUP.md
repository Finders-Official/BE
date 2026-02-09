# GitHub Secrets 정리 체크리스트

마지막 업데이트: 2026-02-09

## 1. 개요 (Overview)
GCP Secret Manager로의 비밀번호 및 환경 변수 마이그레이션이 완료됨에 따라, 더 이상 사용되지 않는 GitHub Secrets를 정리하여 보안을 강화하고 관리를 효율화합니다.

## 2. 삭제 대상 Secrets (Secrets to DELETE) ❌
다음 Secrets는 GCP Secret Manager 또는 Artifact Registry로 대체되었으므로 삭제가 필요합니다.

| Secret 이름 | 설명 | 대체 수단 |
| :--- | :--- | :--- |
| `ENV_PROD` | 운영 환경용 .env 내용 | GCP Secret Manager (`app-prod-*`) |
| `ENV_DEV` | 개발 환경용 .env 내용 | GCP Secret Manager (`app-dev-*`) |
| `DOCKER_USERNAME` | Docker Hub 사용자명 | GCP Artifact Registry |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 | GCP Artifact Registry |
| `CLOUDFLARE_TUNNEL_HOSTNAME` | Cloudflare 터널 호스트명 | 현재 배포 구조에서 미사용 |
| `COMPUTE_SA_EMAIL` | Compute Engine 서비스 계정 이메일 | Terraform 내 정의로 대체 |
| `GCE_HOST` | GCE 호스트 주소 | `GCE_NAME` 기반 배포로 대체 |

## 3. 유지 대상 Secrets (Secrets to KEEP) ✅
다음 Secrets는 CI/CD 워크플로우 및 GCP 인증을 위해 계속 필요합니다.

| Secret 이름 | 이유 |
| :--- | :--- |
| `WIF_PROVIDER` | Workload Identity Federation 제공자 (GCP 인증 필수) |
| `WIF_SERVICE_ACCOUNT` | WIF용 서비스 계정 (GCP 인증 필수) |
| `GCP_PROJECT_ID` | GCP 프로젝트 ID (배포 필수) |
| `GCE_USER` | 배포용 SSH 사용자명 |
| `GCE_NAME` | 배포 대상 인스턴스 이름 |
| `GCE_ZONE` | 배포 대상 인스턴스 존(Zone) |

## 4. 삭제 단계 (Deletion Steps) 📋

### ⚠️ 주의사항
삭제 전 반드시 다음 사항을 확인하세요:
1. 모든 변경 사항이 `develop` 및 `main` 브랜치에 머지되었는지 확인
2. `develop` 브랜치 배포 성공 확인
3. `main` 브랜치 배포 성공 확인

### GitHub UI를 통한 삭제
1. GitHub 저장소 설정으로 이동: [Secrets/Actions Settings](https://github.com/Finders-Official/BE/settings/secrets/actions)
2. 삭제할 Secret 이름을 클릭합니다.
3. **"Remove secret"** 버튼을 클릭합니다.
4. 삭제 확인 팝업에서 승인합니다.

### GitHub CLI (gh)를 통한 삭제
```bash
# GitHub CLI 로그인
gh auth login

# Secrets 삭제 실행
gh secret delete ENV_PROD --repo Finders-Official/BE
gh secret delete ENV_DEV --repo Finders-Official/BE
gh secret delete DOCKER_USERNAME --repo Finders-Official/BE
gh secret delete DOCKER_PASSWORD --repo Finders-Official/BE
gh secret delete CLOUDFLARE_TUNNEL_HOSTNAME --repo Finders-Official/BE
gh secret delete COMPUTE_SA_EMAIL --repo Finders-Official/BE
gh secret delete GCE_HOST --repo Finders-Official/BE
```

## 5. 검증 (Verification) ✅
정리 후 다음 명령어를 통해 필요한 Secrets만 남았는지 확인합니다.

```bash
gh secret list --repo Finders-Official/BE
```

**남아있어야 하는 목록:**
- `WIF_PROVIDER`
- `WIF_SERVICE_ACCOUNT`
- `GCP_PROJECT_ID`
- `GCE_USER`
- `GCE_NAME`
- `GCE_ZONE`

## 6. 복구 절차 (Rollback) 🔄
만약 삭제 후 배포 과정에서 오류가 발생할 경우:
1. 워크플로우 로그에서 누락된 Secret 이름을 확인합니다.
2. GitHub UI를 통해 해당 Secret을 일시적으로 다시 추가합니다.
3. 해당 Secret이 왜 여전히 필요한지 조사하고, 필요한 경우 문서를 업데이트합니다.
