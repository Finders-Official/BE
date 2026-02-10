# IaC와 Terraform 기초 가이드

> 인프라를 코드로 관리하는 개념과 Terraform의 기초를 쉽게 설명

## 목차
1. [IaC(Infrastructure as Code)란?](#1-iacinfrastructure-as-code란)
2. [IaC가 나오게 된 배경](#2-iac가-나오게-된-배경)
3. [Terraform이란?](#3-terraform이란)
4. [핵심 개념: 선언형 vs 명령형](#4-핵심-개념-선언형-vs-명령형)
5. [다른 계정/프로젝트에서도 동일하게?](#5-다른-계정프로젝트에서도-동일하게)
6. [Finders 인프라와 Terraform](#6-finders-인프라와-terraform)
7. [추천 프로젝트 구조](#7-추천-프로젝트-구조)
8. [학습 로드맵](#8-학습-로드맵)
9. [추천 학습 리소스](#9-추천-학습-리소스)
10. [퀴즈](#10-퀴즈)
11. [핵심 정리](#11-핵심-정리)

---

## 1. IaC(Infrastructure as Code)란?

### 건축 설계도 비유
IaC는 말 그대로 **"인프라를 코드로 관리하는 것"**입니다. 이를 건축에 비유하면 다음과 같습니다.

- **Terraform 코드**: 건물의 상세 설계도 (자재, 층수, 배선 등 명시)
- **Cloud Provider (GCP/AWS)**: 설계도대로 건물을 지어주는 시공사
- **인프라**: 실제로 지어진 건물

```
[설계도 (Code)]  ───전달───▶  [시공사 (GCP)]  ───건축───▶  [건물 (Infra)]
```

과거에는 서버를 한 대 늘리려면 관리자가 직접 클라우드 콘솔(GUI)에 들어가서 수십 번 클릭을 해야 했습니다. 하지만 IaC를 사용하면 "서버 1대 추가"라는 코드를 작성하고 실행하기만 하면 됩니다.

### 수동 클릭 vs 코드 관리
| 구분 | 수동 관리 (ClickOps) | 코드 관리 (IaC) |
|------|-------------------|---------------|
| 속도 | 느림 (사람이 직접 클릭) | 빠름 (자동 실행) |
| 정확성 | 실수 가능성 높음 | 코드대로 정확히 생성 |
| 재현성 | 똑같이 만들기 어려움 | 코드만 있으면 100% 재현 |
| 문서화 | 별도 문서 필요 | 코드 자체가 문서 |

---

## 2. IaC가 나오게 된 배경

인프라 관리 방식은 기술의 발전과 함께 변화해 왔습니다.

### 역사 타임라인
```
2000년대 초: 물리 서버 시대
    │   (서버실 랙에 직접 서버를 설치하고 케이블을 연결하던 시절)
    ↓
2006년: AWS EC2 등장
    │   (웹 화면에서 클릭 몇 번으로 서버를 빌려 쓰는 혁명)
    ↓
2008년: GCP, AWS CLI 등장
    │   (명령어로 인프라를 제어하기 시작)
    ↓
2010년: Chef, Puppet 등장
    │   (서버 내부 설정을 코드로 관리하는 '설정 관리' 도구 유행)
    ↓
2014년: Terraform 0.1 출시
    │   (인프라 자체를 선언적으로 관리하는 현대적 IaC의 시작)
    ↓
2024년: OpenTofu 오픈소스 포크
        (더 개방적인 IaC 생태계로 확장)
```

### 왜 IaC가 필요한가요?
1. **사람은 실수합니다**: GUI에서 체크박스 하나 잘못 누르면 보안 사고가 터집니다.
2. **기억은 흐려집니다**: 6개월 전에 만든 서버 설정을 똑같이 재현하기는 불가능에 가깝습니다.
3. **협업이 어렵습니다**: 누가 어떤 설정을 바꿨는지 히스토리를 알기 어렵습니다.

---

## 3. Terraform이란?

Terraform은 HashiCorp에서 만든 가장 대중적인 IaC 도구입니다. **HCL(HashiCorp Configuration Language)**이라는 읽기 쉬운 언어를 사용하여 인프라를 정의합니다.

### 간단한 GCE 리소스 예시
Finders의 API 서버를 Terraform으로 정의하면 다음과 같은 모습이 됩니다.

```hcl
# GCE 인스턴스 정의
resource "google_compute_instance" "app_server" {
  name         = "finders-api-prod"
  machine_type = "e2-medium"
  zone         = "asia-northeast3-a"
  project      = var.project_id # 변수 사용

  boot_disk {
    initialize_params {
      image = "ubuntu-2204-lts"
      size  = 20
    }
  }

  network_interface {
    network    = var.vpc_name
    subnetwork = var.subnet_name
    
    # 공인 IP가 필요한 경우 access_config 추가
    access_config {
      # 빈 블록은 임시 공인 IP 할당을 의미
    }
  }

  tags = ["http-server", "https-server"]
}
```

> **주의**: 실제 프로젝트 ID나 비밀번호는 코드에 직접 적지 않고 `var.project_id`와 같은 변수 처리나 Secret Manager를 사용합니다.

---

## 4. 핵심 개념: 선언형 vs 명령형

Terraform의 가장 큰 특징은 **'선언형(Declarative)'**이라는 점입니다.

### 비유: 택시 타기
- **명령형 (CLI)**: "기사님, 여기서 직진하시다가 다음 신호등에서 우회전해주시고, 100m 가서 세워주세요." (과정을 일일이 설명)
- **선언형 (Terraform)**: "기사님, 강남역으로 가주세요." (최종 목적지만 설명)

### 비교 테이블
| 방식 | 예시 | 특징 |
|------|------|------|
| **명령형 (CLI)** | `gcloud compute instances create ...` | 실행 순서가 중요하며, 똑같은 명령을 두 번 실행하면 에러가 나거나 서버가 두 대 생깁니다. |
| **선언형 (Terraform)** | `resource "google_compute_instance" {...}` | 최종 상태를 정의합니다. 이미 서버가 있다면 아무 일도 하지 않고, 설정이 바뀌었다면 차이점만 수정합니다. |

### Terraform 워크플로우
Terraform은 다음과 같은 3단계 과정을 거쳐 인프라를 변경합니다.

```
1. terraform plan
      │
      ▼
[현재 상태] vs [원하는 상태] 비교
      │
      ▼
차이점 출력 (추가/변경/삭제될 리소스 목록)
      │
      ▼
2. terraform apply
      │
      ▼
[실제 인프라 변경 실행]
      │
      ▼
3. 상태 저장 (terraform.tfstate)
```

---

## 5. 다른 계정/프로젝트에서도 동일하게?

IaC의 핵심 가치는 **재현성(Reproducibility)**입니다.

### 비유: 맥도날드 매뉴얼
전 세계 어느 맥도날드를 가도 빅맥 맛이 똑같은 이유는 상세한 **'레시피 매뉴얼'**이 있기 때문입니다.

- **모듈 (Module)**: 빅맥 레시피 (공통적인 인프라 구조)
- **변수 (Variable)**: 지역별 재료 가격이나 매장 위치 (환경별 차이점)

### 변수 파일 예시 (`terraform.tfvars`)
동일한 코드를 사용하더라도 변수 파일만 바꾸면 개발(dev) 환경과 운영(prod) 환경을 쉽게 분리할 수 있습니다.

```hcl
# 운영 환경 설정 예시
project_id = "finders-prod-12345"
region     = "asia-northeast3"
env        = "prod"
vpc_name   = "finders-vpc-prod"
```

이렇게 관리하면 "개발 서버에서는 잘 되는데 운영 서버에서는 왜 안 되지?"라는 상황을 원천 차단할 수 있습니다.

---

## 6. Finders 인프라와 Terraform

Finders 프로젝트의 인프라는 약 85~90% 정도 Terraform으로 코드화가 가능합니다.

### 리소스별 Terraform 가능 여부
| Resource | Terraform 가능? | Provider | 비고 |
|----------|:---:|----------|------|
| GCP VPC + Subnets | ✅ | `google` | 완전 지원 |
| GCE Instance | ✅ | `google` | 완전 지원 |
| Cloud SQL | ✅ | `google` | 완전 지원 |
| GCS Buckets | ✅ | `google` | 완전 지원 |
| IAM (권한 관리) | ✅ | `google` | 완전 지원 |
| Cloudflare Tunnel | ✅ | `cloudflare` | 완전 지원 |
| 가비아 DNS | ❌ | - | API 미지원으로 수동 관리 |

> **참고**: 자세한 리소스 정보는 [INFRASTRUCTURE.md](../architecture/INFRASTRUCTURE.md)를 참고하세요. 네트워크 기초 지식이 필요하다면 [NETWORK_BASICS.md](./NETWORK_BASICS.md)를 먼저 읽어보시는 것을 추천합니다.

---

## 7. 추천 프로젝트 구조

Terraform 프로젝트는 규모가 커질수록 구조화가 중요합니다. Finders 팀에서는 다음과 같은 구조를 권장합니다.

```
infra/
├── environments/           # 환경별 설정
│   ├── prod/               # 운영 환경
│   │   ├── main.tf         # 모듈 호출 및 리소스 정의
│   │   ├── variables.tf    # 변수 선언
│   │   └── terraform.tfvars # 실제 변수 값 (Secret 제외)
│   └── dev/                # 개발 환경
│       ├── main.tf
│       ├── variables.tf
│       └── terraform.tfvars
└── modules/                # 재사용 가능한 부품들
    ├── networking/         # VPC, Subnet, Firewall
    │   ├── vpc.tf
    │   └── subnets.tf
    ├── compute/            # GCE, Instance Group
    │   └── instances.tf
    ├── database/           # Cloud SQL
    │   └── cloudsql.tf
    └── storage/            # GCS Buckets
        └── buckets.tf
```

---

## 8. 학습 로드맵

Terraform은 한 번에 모든 것을 배우기보다 단계별로 익히는 것이 좋습니다.

### Phase 1: Terraform 기초 (1주)
- [ ] Terraform 공식 튜토리얼(GCP) 완료
- [ ] HCL 문법 (Resource, Variable, Output) 학습
- [ ] `terraform init`, `plan`, `apply`, `destroy` 명령어 실습

### Phase 2: GCP 리소스 Import (2주)
- [ ] 기존에 수동으로 만든 VPC를 코드로 가져오기 (`terraform import`)
- [ ] GCE 인스턴스 설정 코드화
- [ ] Cloud SQL 설정 코드화

### Phase 3: Cloudflare 연동 (1주)
- [ ] Cloudflare Provider 설정 방법 학습
- [ ] Cloudflare Tunnel 설정을 코드로 관리하기

### Phase 4: Secret Manager 연동 (1주)
- [ ] DB 비밀번호 등 민감 정보를 코드에서 분리
- [ ] GCP Secret Manager와 Terraform 연동

### Phase 5: CI/CD 파이프라인 (예정)
- [ ] GitHub Actions를 이용한 자동 `terraform plan`
- [ ] 코드 리뷰 승인 시 자동 `apply` 환경 구축

---

## 9. 추천 학습 리소스

| 리소스 | 링크 | 설명 |
|--------|------|------|
| Terraform 공식 튜토리얼 | [GCP Get Started](https://developer.hashicorp.com/terraform/tutorials/gcp-get-started) | GCP 환경에서 시작하는 가장 빠른 방법 |
| Google Cloud Foundation Toolkit | [GitHub](https://github.com/GoogleCloudPlatform/cloud-foundation-toolkit) | Google에서 권장하는 인프라 모범 사례 코드 |
| Cloudflare Terraform Provider | [Registry](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs) | Cloudflare 리소스 관리 문서 |
| 책: Terraform Up & Running | [O'Reilly](https://www.terraformupandrunning.com/) | 실전 Terraform 운영의 바이블 (Gruntwork 저) |

---

## 10. 퀴즈

### Q1. Terraform의 가장 큰 장점은 무엇인가요?
<details>
<summary>정답 보기</summary>

**재현성(Reproducibility)**

코드만 있으면 어떤 계정이나 프로젝트에서든 동일한 인프라를 100% 똑같이 구축할 수 있습니다. 이는 환경 간 차이로 발생하는 문제를 방지해줍니다.
</details>

### Q2. '선언형' 방식의 특징은 무엇인가요?
<details>
<summary>정답 보기</summary>

**최종 상태(Desired State)를 정의하는 것**

어떻게(How) 인프라를 만들지 과정을 나열하는 것이 아니라, 어떤(What) 상태가 되어야 하는지를 정의합니다. Terraform이 현재 상태와 비교하여 필요한 작업만 자동으로 수행합니다.
</details>

### Q3. Finders 인프라 중 Terraform으로 관리하기 어려운 것은?
<details>
<summary>정답 보기</summary>

**가비아 DNS 설정**

가비아는 현재 Terraform Provider를 공식적으로 지원하지 않거나 API 접근이 제한적이기 때문에, 도메인 연결 설정은 수동으로 관리해야 합니다.
</details>

### Q4. `terraform plan` 명령어의 역할은 무엇인가요?
<details>
<summary>정답 보기</summary>

**변경 사항 미리보기**

실제로 인프라를 수정하기 전에, 코드를 실행했을 때 어떤 리소스가 생성, 수정, 삭제될지 미리 확인하는 단계입니다. 안전한 운영을 위해 필수적인 과정입니다.
</details>

### Q5. 왜 변수(Variable) 파일을 따로 분리해서 관리하나요?
<details>
<summary>정답 보기</summary>

**코드의 재사용성과 보안 때문입니다.**

인프라의 구조(레시피)는 동일하게 유지하면서, 환경별로 다른 값(프로젝트 ID, 리전 등)만 갈아 끼울 수 있습니다. 또한 민감한 정보가 코드에 직접 노출되는 것을 방지할 수 있습니다.
</details>

---

## 11. 핵심 정리

1. **IaC = 인프라를 코드로 관리** (건축 설계도 비유)
2. **Terraform = 선언형 IaC 도구** (최종 목적지만 말하면 알아서 해줌)
3. **재현성 = 코드만 있으면 어디서든 동일** (맥도날드 매뉴얼 비유)
4. **Finders 인프라 85-90% 코드화 가능** (가비아 DNS 등 일부 제외)
5. **학습 순서: 기초 → Import → Cloudflare → Secret → CI/CD**

---

**다음 단계**: [INFRASTRUCTURE.md](../architecture/INFRASTRUCTURE.md)에서 현재 리소스 구성을 확인한 후, Terraform 공식 튜토리얼을 따라 실습해보세요.

**마지막 업데이트**: 2026-02-09
