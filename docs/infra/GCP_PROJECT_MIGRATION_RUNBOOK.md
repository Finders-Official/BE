# GCP 프로젝트 마이그레이션 런북

Finders 인프라를 다른 GCP 프로젝트로 이전할 때 코드 수정 없이 GitHub Secrets 값만 교체하는 운영 절차입니다.

## 목적

- Terraform backend bucket을 코드에서 분리해 마이그레이션 시 코드 변경을 없앱니다.
- CI/CD가 참조하는 상태 버킷을 `TF_STATE_BUCKET` 하나로 통일합니다.

## 사전 조건

- 대상 GCP 프로젝트에 Terraform state 버킷이 생성되어 있어야 합니다.
- GitHub Actions WIF 서비스 계정이 대상 프로젝트/버킷에 접근 가능해야 합니다.
- `terraform.tfvars`가 대상 프로젝트 값으로 준비되어 있어야 합니다.

## GitHub Secrets 교체 항목

마이그레이션 시 아래 4개 시크릿만 변경합니다.

1. `GCP_PROJECT_ID`
2. `WIF_PROVIDER`
3. `WIF_SERVICE_ACCOUNT`
4. `TF_STATE_BUCKET`

## 단계별 절차

### 1) 대상 프로젝트 준비

```bash
gcloud config set project <NEW_PROJECT_ID>
gcloud storage buckets create gs://<NEW_PROJECT_ID>-tf-state --location=asia-northeast3
```

### 2) terraform.tfvars 업로드

```bash
cd infra
gcloud storage cp terraform.tfvars gs://<NEW_PROJECT_ID>-tf-state/terraform.tfvars
```

### 3) GitHub Secrets 업데이트

- GitHub Repository Settings -> Secrets and variables -> Actions
- 위 4개 시크릿을 대상 프로젝트 값으로 교체

### 4) 검증

1. `develop` 대상 PR에서 Terraform workflow가 `init/plan` 성공하는지 확인
2. `develop` 머지 후 push 이벤트에서 Terraform `apply`가 성공하는지 확인
3. 배포 워크플로우가 `deploy-config.json`을 정상 로드하는지 확인

## 롤백 절차

이상 발생 시 이전 프로젝트의 4개 시크릿 값으로 되돌리고 Terraform workflow를 재실행합니다.

## 체크리스트

- [ ] 대상 프로젝트 state 버킷 생성
- [ ] `terraform.tfvars` 업로드
- [ ] GitHub Secrets 4개 교체
- [ ] Terraform PR plan 성공
- [ ] develop merge 후 apply 성공
- [ ] deploy/dev-deploy 파이프라인 정상 동작
