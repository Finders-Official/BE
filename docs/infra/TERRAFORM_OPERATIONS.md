# Terraform 운영 가이드

Finders 인프라를 Terraform으로 관리하기 위한 팀 운영 가이드입니다.

## 목차

- [일상 워크플로우](#일상-워크플로우)
- [안전 수칙](#안전-수칙)
- [긴급 상황 대응](#긴급-상황-대응)
- [새 팀원 온보딩](#새-팀원-온보딩)
- [트러블슈팅](#트러블슈팅)

---

## 일상 워크플로우

### 인프라 변경 프로세스

1. **이슈 생성**
   ```bash
   gh issue create --title "[INFRA] 변경 내용 요약" --label infra
   ```

2. **브랜치 생성**
   ```bash
   git checkout develop
   git pull
   git checkout -b infra/description-#<ISSUE>
   ```

3. **Terraform 코드 수정**
   ```bash
   cd infra
   # .tf 파일 수정
   terraform fmt
   terraform validate
   ```

4. **로컬 Plan 확인**
   ```bash
   terraform plan
   # 변경사항 검토 - 예상과 일치하는지 확인
   ```

5. **PR 생성**
   ```bash
   git add infra/
   git commit -m "infra: 변경 내용 요약 (#<ISSUE>)"
   git push -u origin infra/description-#<ISSUE>
   gh pr create --base develop
   ```

6. **CI에서 Plan 확인**
   - GitHub Actions가 자동으로 `terraform plan` 실행
   - PR 코멘트에 plan 결과 표시
   - 팀원 리뷰 + plan 결과 확인

7. **Merge 후 자동 Apply**
   - develop 브랜치에 merge되면 자동으로 `terraform apply` 실행
   - Actions 로그에서 apply 결과 확인

---

## 안전 수칙

### 🚨 절대 금지 사항

1. **로컬에서 `terraform apply` 금지**
   - CI/CD를 통해서만 apply
   - 예외: 긴급 상황 + 팀 리더 승인

2. **`prevent_destroy` 제거 금지**
   - GCE, Cloud SQL, GCS, VPC에 설정됨
   - 제거 시 실수로 리소스 삭제 가능

3. **시크릿 커밋 금지**
   - `terraform.tfvars` 절대 커밋 금지 (gitignore됨)
   - API 토큰, 비밀번호 등 `.tf` 파일에 하드코딩 금지

4. **Plan 없이 Apply 금지**
   - 항상 plan 결과 확인 후 apply
   - CI/CD는 자동으로 이 규칙 준수

### ✅ 필수 체크리스트

변경 전 반드시 확인:
- [ ] `terraform plan` 결과가 예상과 일치하는가?
- [ ] ForceNew 속성 변경이 있는가? (리소스 재생성 = 다운타임)
- [ ] `prevent_destroy`가 설정된 리소스를 삭제하려는가?
- [ ] 시크릿이 코드에 포함되어 있지 않은가?
- [ ] 팀원 리뷰를 받았는가?

---

## 긴급 상황 대응

### State 잠금 해제

Terraform apply 중 중단되어 state가 잠긴 경우:

```bash
cd infra
terraform force-unlock <LOCK_ID>
# LOCK_ID는 에러 메시지에 표시됨
```

⚠️ **주의**: 다른 사람이 apply 중일 수 있으므로 팀에 확인 후 실행

### State 롤백

잘못된 apply 후 이전 상태로 복구:

```bash
# GCS 버킷에서 이전 버전 확인
gcloud storage ls -l gs://<TF_STATE_BUCKET>/terraform/state/

# 이전 버전 다운로드
gcloud storage cp gs://<TF_STATE_BUCKET>/terraform/state/default.tfstate#<VERSION> ./terraform.tfstate

# State 복구
cd infra
terraform state push terraform.tfstate
```

### 리소스 수동 복구

Terraform 외부에서 리소스가 변경된 경우:

```bash
cd infra
terraform refresh  # 실제 상태를 state에 반영
terraform plan     # drift 확인
# 필요시 .tf 파일 수정하여 실제 상태와 일치시킴
```

---

## 새 팀원 온보딩

### 1. Terraform 설치

```bash
# macOS
brew install terraform

# Linux
wget https://releases.hashicorp.com/terraform/1.5.0/terraform_1.5.0_linux_amd64.zip
unzip terraform_1.5.0_linux_amd64.zip
sudo mv terraform /usr/local/bin/

# 버전 확인
terraform version  # 1.5.0 이상
```

### 2. GCP 인증 설정

```bash
# gcloud CLI 설치 (https://cloud.google.com/sdk/docs/install)
gcloud auth application-default login
gcloud config set project <PROJECT_ID>
```

### 3. Terraform 변수 설정

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 편집 (팀 리더에게 실제 값 요청)
```

### 4. 초기화 및 테스트

```bash
cd infra
terraform init -backend-config="bucket=<TF_STATE_BUCKET>"
terraform plan  # 변경사항 없어야 함 (No changes)
```

---

## 트러블슈팅

### "Error: Failed to open state file"

**원인**: GCS state 버킷 접근 권한 없음

**해결**:
```bash
# 권한 확인
gcloud storage buckets get-iam-policy gs://<TF_STATE_BUCKET>

# 또는 환경 변수 기반
gcloud storage buckets get-iam-policy gs://$TF_STATE_BUCKET
```

### "Error: Resource already exists"

**원인**: 리소스가 이미 존재하는데 Terraform이 생성하려고 함

**해결**:
```bash
# 기존 리소스를 import
terraform import <RESOURCE_TYPE>.<NAME> <RESOURCE_ID>
```

### "Error: ... has been modified"

**원인**: Terraform 외부에서 리소스가 변경됨 (drift)

**해결**:
```bash
terraform refresh  # 실제 상태 반영
terraform plan     # drift 확인
# .tf 파일을 실제 상태에 맞게 수정
```

### Plan에서 예상치 못한 변경사항

**원인**: 코드와 실제 인프라 상태 불일치

**해결**:
1. `terraform show` 로 현재 state 확인
2. `gcloud` 명령어로 실제 리소스 상태 확인
3. `.tf` 파일을 실제 상태에 맞게 조정
4. `terraform plan` 으로 변경사항 0 확인

---

## 참고 자료

- [Terraform 공식 문서](https://developer.hashicorp.com/terraform/docs)
- [Google Provider 문서](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
- [Cloudflare Provider 문서](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs)
- [IaC/Terraform 개념 학습](./IAC_TERRAFORM_INTRO.md)
- [GCP 프로젝트 마이그레이션 런북](./GCP_PROJECT_MIGRATION_RUNBOOK.md)
- [인프라 아키텍처](../architecture/INFRASTRUCTURE.md)

---

## 연락처

Terraform 관련 질문이나 긴급 상황 시:
- Slack: #infra 채널
- 담당자: [팀 리더 이름]
