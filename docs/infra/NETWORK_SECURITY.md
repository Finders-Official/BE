# 네트워크 및 보안 설정 가이드

> Finders API 서버의 네트워크 구성과 보안 설정에 대한 가이드

## 목차
- [현재 상태](#현재-상태)
- [보안 계층 이해하기](#보안-계층-이해하기)
- [로드맵](#로드맵)
- [단계별 설정 가이드](#단계별-설정-가이드)

---

## 현재 상태

### 구성 요소

| 항목 | 상태 | 설명 |
|------|------|------|
| **도메인** | ✅ 구매 완료 | finders.it.kr (가비아) |
| **Cloudflare** | ✅ Zero Trust Tunnel 사용 중 | 무료 플랜 |
| **GCP Compute Engine** | ✅ 운영 중 | e2-medium, 서울 리전 |
| **VPC** | ⚠️ 기본 설정 | 커스텀 설정 필요 |
| **방화벽** | ⚠️ 기본 설정 | 최소 권한 원칙 적용 필요 |
| **nginx** | ❓ 확인 필요 | 블루-그린 배포용 |

### 현재 네트워크 흐름

```
[사용자]
  ↓
[Cloudflare Edge Network]
  ↓ (암호화된 터널)
[cloudflared 데몬] (Compute Engine 내부)
  ↓
[Spring Boot :8080]
  ↓
[Cloud SQL]
```

---

## 보안 계층 이해하기

### 1단계: Cloudflare (가장 바깥쪽)

**역할**: 인터넷 공격으로부터 서버 보호

```
[전 세계 공격자들] → DDoS, 봇, SQL Injection 시도
           ↓
    [Cloudflare Edge]
    - DDoS 방어 (무제한)
    - 봇 차단
    - WAF (웹 방화벽)
    - SSL/TLS 암호화
           ↓ 정상 트래픽만 통과
    [당신의 서버]
```

**설정 위치**: https://one.dash.cloudflare.com/
**비용**: 무료 (50명까지)
**효과**: 서버 IP 숨김, 초당 수백만 공격 차단

---

### 2단계: GCP VPC (네트워크 격리)

**역할**: 리소스 간 네트워크 격리

```
┌─────────────── VPC: finders-vpc ───────────────┐
│                                                  │
│  Public Subnet                                  │
│  ├─ Compute Engine (Web)                       │
│  └─ Bastion Host (SSH 접속용)                  │
│                                                  │
│  Private Subnet                                 │
│  ├─ Cloud SQL (DB)                             │
│  └─ 내부 서비스                                 │
│                                                  │
└──────────────────────────────────────────────────┘
```

**필요성**:
- 외부에서 DB 직접 접근 차단
- 서비스 간 통신 제어
- 보안 규정 준수

**현재 상태**: 기본 VPC 사용 중 (커스텀 설정 예정)

---

### 3단계: Compute Engine 방화벽 (포트 제어)

**역할**: 허용된 포트만 열기

```
[인터넷]
    ↓
[방화벽 규칙]
- 22 (SSH): 내 IP만 허용 ✅
- 80 (HTTP): Cloudflare만 허용 ✅
- 443 (HTTPS): Cloudflare만 허용 ✅
- 3306 (MySQL): 차단 ❌ (내부만)
- 8080 (Spring): 차단 ❌ (내부만)
    ↓
[서버]
```

**현재 상태**: 기본 설정, 최소 권한 적용 필요

---

### 4단계: nginx (리버스 프록시)

**역할**: 블루-그린 배포, 요청 라우팅

```
[Cloudflare Tunnel]
    ↓
[nginx :80]
    ├─ /api/* → Blue 컨테이너 :8080
    └─ /health → 헬스체크
```

**필요성**:
- 무중단 배포
- 요청 속도 제한
- URL 기반 라우팅

**현재 상태**: 확인 필요

---

### 5단계: Spring Security (애플리케이션)

**역할**: 비즈니스 로직 보안

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() { ... }

// JWT 인증
// 입력 값 검증
// SQL Injection 방지
```

**현재 상태**: 구현 완료

---

## 로드맵

### Phase 1: 현재 (MVP) ✅
```
[Cloudflare Tunnel]
    ↓
[Compute Engine - 기본 설정]
    ↓
[Spring Boot]
```

**특징**:
- 빠른 배포
- 기본 보안
- 최소 비용

---

### Phase 2: 안정화 (진행 중) ⚠️
```
[Cloudflare Tunnel]
    ↓
[nginx - 블루-그린 배포]
    ├─ Blue 컨테이너
    └─ Green 컨테이너
```

**목표**:
- 무중단 배포
- 기본 방화벽 강화
- 접속 로그 관리

**예상 시기**: 1~2주

---

### Phase 3: 프로덕션 준비 (예정) 📋
```
[Cloudflare Zero Trust]
    ↓
┌──────── VPC ────────┐
│ Public Subnet       │
│  ├─ nginx          │
│  └─ Bastion        │
│                     │
│ Private Subnet      │
│  ├─ App Servers    │
│  └─ Cloud SQL      │
└─────────────────────┘
```

**목표**:
- VPC 커스텀 설정
- Private Subnet으로 DB 격리
- 팀원 접속 관리

**예상 시기**: 서비스 오픈 전

---

### Phase 4: 스케일링 (미래) 🚀
```
[Cloudflare + CDN]
    ↓
[Cloud Load Balancer]
    ├─ App Server 1 (AZ-A)
    ├─ App Server 2 (AZ-B)
    └─ App Server 3 (AZ-C)
```

**목표**:
- 자동 스케일링
- 멀티 가용영역
- 고가용성

**예상 시기**: 서비스 안정화 후

---

## 단계별 설정 가이드

### 1단계: 도메인 연결 (완료) ✅

#### 가비아 DNS 설정
```
Type: A
Host: @
Value: [Cloudflare IP]
TTL: 600
```

#### Cloudflare DNS 설정
```
Type: CNAME
Name: api
Target: [터널 주소].cfargotunnel.com
Proxy: Proxied (주황색)
```

---

### 2단계: Cloudflare Tunnel 설정 (완료) ✅

#### 설치 확인
```bash
# Compute Engine에서 확인
sudo systemctl status cloudflared
```

#### 터널 설정 확인
```bash
cat /etc/cloudflared/config.yml
```

예상 내용:
```yaml
tunnel: [TUNNEL-ID]
credentials-file: /root/.cloudflared/[TUNNEL-ID].json

ingress:
  - hostname: api.finders.it.kr
    service: http://localhost:8080

  - service: http_status:404
```

---

### 3단계: 방화벽 최소 권한 설정 (예정) ⚠️

#### 현재 규칙 확인
```bash
gcloud compute firewall-rules list
```

#### 권장 규칙

**삭제할 규칙** (너무 개방적):
```bash
# 모든 IP에서 80/443 허용하는 규칙 삭제
gcloud compute firewall-rules delete allow-http-https
```

**추가할 규칙** (최소 권한):
```bash
# 1. SSH: 내 IP만 허용
gcloud compute firewall-rules create allow-ssh-my-ip \
  --network=default \
  --allow=tcp:22 \
  --source-ranges=[내-IP]/32 \
  --description="SSH from my IP only"

# 2. Cloudflare Tunnel은 outbound만 사용
# 인바운드 포트 오픈 불필요!

# 3. 내부 통신 허용 (VPC 내부)
gcloud compute firewall-rules create allow-internal \
  --network=default \
  --allow=tcp,udp,icmp \
  --source-ranges=10.128.0.0/9
```

---

### 4단계: VPC 커스텀 설정 (예정) 📋

#### 목표 구조
```
finders-vpc (10.0.0.0/16)
  ├─ finders-public (10.0.1.0/24)
  │   ├─ Compute Engine (웹)
  │   └─ Bastion Host
  │
  └─ finders-private (10.0.2.0/24)
      ├─ Cloud SQL
      └─ 내부 서비스
```

#### 생성 명령어 (나중에 실행)
```bash
# 1. VPC 생성
gcloud compute networks create finders-vpc \
  --subnet-mode=custom

# 2. Public Subnet
gcloud compute networks subnets create finders-public \
  --network=finders-vpc \
  --region=asia-northeast3 \
  --range=10.0.1.0/24

# 3. Private Subnet
gcloud compute networks subnets create finders-private \
  --network=finders-vpc \
  --region=asia-northeast3 \
  --range=10.0.2.0/24 \
  --enable-private-ip-google-access

# 4. Cloud NAT (Private에서 외부 요청용)
gcloud compute routers create finders-router \
  --network=finders-vpc \
  --region=asia-northeast3

gcloud compute routers nats create finders-nat \
  --router=finders-router \
  --region=asia-northeast3 \
  --nat-all-subnet-ip-ranges \
  --auto-allocate-nat-external-ips
```

**⚠️ 주의**: VPC 변경은 서비스 중단을 동반하므로 계획된 점검 시간에 진행

---

### 5단계: Bastion Host 설정 (선택)

#### 언제 필요한가?
- Private Subnet의 서버에 SSH 접속 필요할 때
- 현재는 Cloudflare Tunnel로 대체 가능

#### Cloudflare Zero Trust로 대체 (권장)
```bash
# 팀원이 Private DB에 접속
# Bastion 없이 Cloudflare를 통해 안전하게 접속
mysql -h db.finders.internal -u finders -p
```

**장점**:
- Bastion Host VM 비용 절감 (~$7/월)
- Public IP 불필요
- 이메일 기반 인증
- 자동 로그 기록

---

## 보안 체크리스트

### 현재 필수 (즉시 확인)
- [ ] Cloudflare Tunnel 정상 작동 확인
- [ ] SSH 포트 본인 IP만 허용
- [ ] Cloud SQL 공개 IP 제거 (Private IP 사용)
- [ ] Spring Security JWT 설정 확인
- [ ] 환경 변수 (.env) Git에 커밋 안 됨 확인

### 단기 (1~2주 내)
- [ ] 방화벽 규칙 최소 권한으로 변경
- [ ] nginx 블루-그린 배포 설정
- [ ] 접속 로그 수집 (Cloudflare Logs)
- [ ] SSL 인증서 자동 갱신 확인

### 중기 (서비스 오픈 전)
- [ ] VPC 커스텀 설정
- [ ] Private Subnet으로 DB 이전
- [ ] Bastion/Cloudflare Zero Trust 설정
- [ ] 백업 자동화
- [ ] 모니터링 대시보드 구축

### 장기 (운영 안정화 후)
- [ ] 멀티 가용영역 구성
- [ ] 자동 스케일링
- [ ] 재해 복구 계획 (DR)
- [ ] 보안 침투 테스트

---

## 자주 묻는 질문 (FAQ)

### Q1. VPC를 꼭 설정해야 하나요?
**A**: 초기에는 기본 VPC로도 충분합니다. 하지만 서비스 오픈 전에는 보안 강화를 위해 커스텀 VPC 권장.

### Q2. Bastion Host vs Cloudflare Zero Trust?
**A**: Cloudflare Zero Trust 권장. 비용 절감 + 더 쉬운 관리 + 더 나은 보안.

### Q3. nginx가 꼭 필요한가요?
**A**: 블루-그린 배포를 하려면 필요. 단순 배포는 Spring Boot만으로도 가능.

### Q4. 80/443 포트를 열어야 하나요?
**A**: Cloudflare Tunnel 사용 시 불필요. Tunnel이 outbound 연결만 사용.

### Q5. 방화벽 규칙이 너무 복잡해요
**A**: 최소 원칙: "SSH만 내 IP, 나머지 차단". Cloudflare Tunnel이 알아서 처리.

---

## 학습 자료

### 기초 개념
- [ ] IP 주소와 포트 번호
- [ ] 공인 IP vs 사설 IP
- [ ] 방화벽의 역할
- [ ] VPN vs Proxy

### 클라우드 네트워크
- [ ] VPC 개념
- [ ] Subnet (Public/Private)
- [ ] NAT Gateway
- [ ] Load Balancer

### 보안
- [ ] DDoS 공격과 방어
- [ ] SQL Injection
- [ ] XSS (Cross-Site Scripting)
- [ ] OWASP Top 10

### 실습
- [ ] GCP VPC 생성 튜토리얼
- [ ] Cloudflare Tunnel 설정
- [ ] nginx 리버스 프록시 구성
- [ ] 방화벽 규칙 설정

---

## 참고 문서

- [INFRASTRUCTURE.md](./INFRASTRUCTURE.md) - GCP 리소스 정보
- [GCP_LOGGING_GUIDE.md](./GCP_LOGGING_GUIDE.md) - 로깅 설정
- [LOCAL_DEVELOPMENT.md](../guides/LOCAL_DEVELOPMENT.md) - 로컬 개발 환경

---

## 문의 및 업데이트

- **문서 최종 수정**: 2025-01-19
- **다음 검토 예정**: VPC 설정 후
- **담당자**: DevOps 팀

네트워크 구성 변경 시 이 문서를 반드시 업데이트하세요.
