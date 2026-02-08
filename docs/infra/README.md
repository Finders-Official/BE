# 인프라 문서 인덱스

> Finders API 서버의 인프라 및 네트워크 문서 모음

## 📖 문서 목록

### 🚀 시작하기

| 문서 | 설명 | 대상 |
|------|------|------|
| [NETWORK_BASICS.md](./NETWORK_BASICS.md) | 네트워크 기초 개념 학습 | 초보자 |
| [NETWORK_SECURITY.md](./NETWORK_SECURITY.md) | 네트워크 및 보안 설정 가이드 | 모든 개발자 |
| [NETWORK_CHEATSHEET.md](./NETWORK_CHEATSHEET.md) | 자주 쓰는 명령어 모음 | 모든 개발자 |
| [IAC_TERRAFORM_INTRO.md](./IAC_TERRAFORM_INTRO.md) | IaC/Terraform 개념 학습 | 모든 개발자 |

### 📋 상세 문서

| 문서 | 설명 | 대상 |
|------|------|------|
| [../architecture/INFRASTRUCTURE.md](../architecture/INFRASTRUCTURE.md) | GCP 리소스 정보 | DevOps |
| [GCP_LOGGING_GUIDE.md](./GCP_LOGGING_GUIDE.md) | 로깅 설정 가이드 | DevOps |
| [TERRAFORM_OPERATIONS.md](./TERRAFORM_OPERATIONS.md) | Terraform 운영 가이드 | DevOps |

---

## 🛠️ Terraform 운영

### 개요
Finders 인프라는 Terraform으로 코드화되어 있습니다. 모든 인프라 변경은 Terraform을 통해 관리됩니다.

### 관리 대상 리소스
- GCS 버킷 (finders-public, finders-private)
- IAM 바인딩 (팀원 권한, 서비스 계정)
- VPC 네트워크 (finders-vpc, 3개 서브넷, 6개 방화벽 규칙)
- Cloud SQL (finders-db, 2개 데이터베이스)
- GCE 인스턴스 (finders-server-v2)
- Cloudflare Tunnel (finders-api) — Phase 5 pending

### 주요 문서
- **[Terraform 운영 가이드](./TERRAFORM_OPERATIONS.md)** — 일상 워크플로우, 안전 수칙, 긴급 대응
- **[IaC/Terraform 개념](./IAC_TERRAFORM_INTRO.md)** — Terraform 학습 자료
- **[인프라 아키텍처](../architecture/INFRASTRUCTURE.md)** — 전체 인프라 구조

### 빠른 시작
```bash
# 1. Terraform 설치 (1.5.0+)
brew install terraform

# 2. GCP 인증
gcloud auth application-default login

# 3. 변수 설정
cd infra
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 편집

# 4. 초기화
terraform init

# 5. Plan 확인
terraform plan  # No changes 확인
```

### CI/CD
- PR 생성 시: 자동으로 `terraform plan` 실행, 결과를 PR 코멘트로 표시
- develop 머지 시: 자동으로 `terraform apply` 실행
- Workflow: `.github/workflows/terraform.yml`

### 주의사항
- ⚠️ 로컬에서 `terraform apply` 금지 (CI/CD만 사용)
- ⚠️ `prevent_destroy` 제거 금지
- ⚠️ `terraform.tfvars` 커밋 금지

---

## 🎯 상황별 가이드

### "네트워크가 처음이에요"
1. [NETWORK_BASICS.md](./NETWORK_BASICS.md) 읽기
2. [NETWORK_SECURITY.md](./NETWORK_SECURITY.md)의 "보안 계층 이해하기" 섹션
3. 간단한 실습 (ping, dig 등)

**예상 시간**: 2~3시간

---

### "서버 배포 준비 중이에요"
1. [NETWORK_SECURITY.md](./NETWORK_SECURITY.md)의 "현재 상태" 확인
2. "보안 체크리스트" 따라하기
3. [NETWORK_CHEATSHEET.md](./NETWORK_CHEATSHEET.md)에서 필요한 명령어 찾기

**예상 시간**: 1~2일

---

### "방화벽 설정이 필요해요"
1. [NETWORK_SECURITY.md](./NETWORK_SECURITY.md)의 "3단계: Compute Engine 방화벽" 읽기
2. [NETWORK_CHEATSHEET.md](./NETWORK_CHEATSHEET.md)의 "방화벽 관련" 섹션
3. 명령어 실행

**예상 시간**: 30분

---

### "VPC 설정을 하고 싶어요"
1. [NETWORK_BASICS.md](./NETWORK_BASICS.md)의 "VPC" 섹션 이해
2. [NETWORK_SECURITY.md](./NETWORK_SECURITY.md)의 "4단계: VPC 커스텀 설정"
3. 주의: 서비스 중단 가능, 계획된 점검 시간에 진행

**예상 시간**: 반나절

---

### "Cloudflare Tunnel 문제 해결"
1. [NETWORK_CHEATSHEET.md](./NETWORK_CHEATSHEET.md)의 "문제 해결" 섹션
2. [NETWORK_SECURITY.md](./NETWORK_SECURITY.md)의 "Cloudflare" 섹션
3. 해결 안 되면 팀에 문의

**예상 시간**: 10~30분

---

### "인프라를 코드로 관리하고 싶어요"
1. [IAC_TERRAFORM_INTRO.md](./IAC_TERRAFORM_INTRO.md) 읽기
2. [INFRASTRUCTURE.md](../architecture/INFRASTRUCTURE.md)에서 현재 리소스 확인
3. Terraform 공식 튜토리얼 실습

**예상 시간**: 1주 (기초) ~ 한 달 (실전 적용)

---

## 🔗 빠른 링크

### 외부 대시보드
- [Cloudflare Dashboard](https://dash.cloudflare.com/)
- [GCP Console](https://console.cloud.google.com/)
- [가비아 DNS 관리](https://customer.gabia.com/)

### 상태 페이지
- [Cloudflare Status](https://www.cloudflarestatus.com/)
- [GCP Status](https://status.cloud.google.com/)

### 학습 자료
- [생활코딩 - 네트워크](https://opentutorials.org/course/1)
- [GCP 공식 문서](https://cloud.google.com/docs)
- [Cloudflare 공식 문서](https://developers.cloudflare.com/)

---

## 📊 현재 인프라 상태

### 운영 중
- Cloudflare Zero Trust Tunnel
- GCP Compute Engine (e2-medium)
- Cloud SQL (MySQL 8.0)
- Cloud Storage (public/private)

### 설정 예정
- 방화벽 최소 권한 설정
- nginx 블루-그린 배포
- VPC 커스텀 설정

### 고려 중
- 멀티 가용영역
- 자동 스케일링
- 백업 자동화

---

## 🆘 긴급 상황

### 서비스 장애 시 순서
1. [Health Check](https://api.finders.it.kr/health)
2. [Cloudflare Dashboard](https://dash.cloudflare.com/) 확인
3. [GCP Console](https://console.cloud.google.com/) 확인
4. [NETWORK_CHEATSHEET.md](./NETWORK_CHEATSHEET.md) 문제 해결 섹션
5. 팀에 알림

### 자주 발생하는 문제
| 증상 | 원인 | 해결 |
|------|------|------|
| 사이트 접속 안 됨 | Cloudflare Tunnel 중단 | `sudo systemctl restart cloudflared` |
| DB 연결 실패 | Cloud SQL 중단 | GCP Console에서 인스턴스 확인 |
| SSH 접속 안 됨 | 방화벽 또는 IP 변경 | 방화벽 규칙 확인 |

---

## 📝 문서 업데이트 가이드

### 언제 업데이트하나요?
- VPC 설정 변경 시
- 방화벽 규칙 추가/삭제 시
- Cloudflare 설정 변경 시
- 새로운 서비스 추가 시

### 어떻게 업데이트하나요?
1. 해당 문서 파일 수정
2. "마지막 업데이트" 날짜 변경
3. 변경 사항 커밋
4. 팀에 공유

---

## 🎓 학습 로드맵

### 1단계: 기초 (1주)
- [ ] [NETWORK_BASICS.md](./NETWORK_BASICS.md) 전체 읽기
- [ ] IP, 포트, 방화벽 개념 이해
- [ ] 간단한 실습 (ping, dig)

### 2단계: 클라우드 (1주)
- [ ] VPC 개념 이해
- [ ] Subnet, CIDR 이해
- [ ] GCP Console 둘러보기

### 3단계: 보안 (1주)
- [ ] [NETWORK_SECURITY.md](./NETWORK_SECURITY.md) 읽기
- [ ] 보안 체크리스트 확인
- [ ] 방화벽 규칙 연습

### 4단계: 실전 (진행 중)
- [ ] Cloudflare Tunnel 설정
- [ ] nginx 설정
- [ ] 모니터링 설정

### 5단계: IaC/자동화 (예정)
- [ ] [IAC_TERRAFORM_INTRO.md](./IAC_TERRAFORM_INTRO.md) 읽기
- [ ] Terraform 공식 튜토리얼 실습
- [ ] 기존 리소스 Import 연습
- [ ] Terraform 프로젝트 구조 설계

---

## 💬 피드백

문서에 대한 피드백이나 추가 요청 사항이 있으면:
- GitHub Issue 생성
- 팀 채팅에 공유
- 직접 문서 수정 후 PR

---

## 마지막 업데이트

- **마지막 업데이트**: 2026-02-09
- **작성자**: DevOps 팀

---

## 🗂️ 파일 구조

```
docs/
├─ infra/
│   ├─ README.md (이 파일)
│   ├─ NETWORK_BASICS.md (네트워크 기초)
│   ├─ NETWORK_SECURITY.md (보안 가이드)
│   ├─ NETWORK_CHEATSHEET.md (명령어 모음)
│   ├─ GCP_LOGGING_GUIDE.md (로깅)
│   ├─ IAC_TERRAFORM_INTRO.md (IaC/Terraform 개념)
│   └─ TERRAFORM_OPERATIONS.md (Terraform 운영)
│
└─ architecture/
    ├─ INFRASTRUCTURE.md (GCP 리소스)
    ├─ ARCHITECTURE.md (애플리케이션 구조)
    └─ ERD.md (데이터베이스)
```

**마지막 업데이트**: 2026-02-09
