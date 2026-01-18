# 네트워크 & 보안 치트시트

> 자주 사용하는 명령어와 확인 사항 모음

## 🔍 현재 상태 빠른 확인

### Cloudflare Tunnel 상태
```bash
# 터널 실행 상태
sudo systemctl status cloudflared

# 터널 로그 확인
sudo journalctl -u cloudflared -f

# 터널 재시작
sudo systemctl restart cloudflared

# 설정 파일 확인
cat /etc/cloudflared/config.yml
```

### GCP 방화벽 확인
```bash
# 모든 방화벽 규칙 보기
gcloud compute firewall-rules list

# 특정 규칙 상세 보기
gcloud compute firewall-rules describe RULE_NAME

# 현재 인스턴스에 적용된 태그 확인
gcloud compute instances describe finders-server \
  --zone=asia-northeast3-a \
  --format="value(tags.items)"
```

### Spring Boot 상태
```bash
# 앱 실행 확인
curl http://localhost:8080/health

# 포트 사용 확인
sudo netstat -tlnp | grep :8080

# 프로세스 확인
ps aux | grep java
```

### Cloud SQL 연결 테스트
```bash
# 내부 IP로 연결 (VPC 내부에서)
mysql -h 10.x.x.x -u finders -p

# 공개 IP로 연결 (승인된 네트워크에서만)
mysql -h 34.64.50.136 -u finders -p
```

---

## 🔐 보안 점검 체크리스트

### 매일 확인
```bash
# 1. Cloudflare Tunnel 정상 작동
sudo systemctl status cloudflared

# 2. Spring Boot 헬스체크
curl https://api.finders.it.kr/health

# 3. 디스크 용량
df -h

# 4. 메모리 사용량
free -h
```

### 매주 확인
```bash
# 1. 시스템 업데이트
sudo apt update
sudo apt list --upgradable

# 2. 로그 확인
sudo journalctl -u cloudflared --since "1 week ago" | grep ERROR
sudo journalctl -u spring-boot --since "1 week ago" | grep ERROR

# 3. 방화벽 규칙 검토
gcloud compute firewall-rules list
```

### 매월 확인
- [ ] GCP 비용 확인
- [ ] SSL 인증서 만료일 (Cloudflare 자동 갱신 확인)
- [ ] 백업 정상 작동 확인
- [ ] 미사용 리소스 정리

---

## 🛠️ 자주 쓰는 작업

### 도메인 관련

#### DNS 전파 확인
```bash
# 도메인이 Cloudflare를 가리키는지 확인
dig api.finders.it.kr

# 또는
nslookup api.finders.it.kr
```

#### SSL 인증서 확인
```bash
# 인증서 정보 보기
openssl s_client -connect api.finders.it.kr:443 -servername api.finders.it.kr < /dev/null | openssl x509 -noout -dates
```

---

### 방화벽 관련

#### SSH 접근 제한 (내 IP만)
```bash
# 현재 내 IP 확인
curl ifconfig.me

# 방화벽 규칙 생성
gcloud compute firewall-rules create allow-ssh-my-ip \
  --network=default \
  --allow=tcp:22 \
  --source-ranges=$(curl -s ifconfig.me)/32 \
  --description="SSH from my IP only"
```

#### HTTP/HTTPS 포트 차단 (Tunnel 사용 시)
```bash
# 기존 규칙 삭제
gcloud compute firewall-rules delete allow-http
gcloud compute firewall-rules delete allow-https

# 또는 비활성화
gcloud compute firewall-rules update allow-http --disabled
```

#### 특정 IP만 허용
```bash
# Cloudflare IP 범위만 허용 (선택사항)
gcloud compute firewall-rules create allow-cloudflare \
  --network=default \
  --allow=tcp:80,tcp:443 \
  --source-ranges=173.245.48.0/20,103.21.244.0/22 \
  --description="Allow Cloudflare IPs only"
```

---

### VPC 관련 (나중에 사용)

#### VPC 생성
```bash
# 커스텀 VPC 생성
gcloud compute networks create finders-vpc \
  --subnet-mode=custom \
  --bgp-routing-mode=regional

# Public Subnet
gcloud compute networks subnets create finders-public \
  --network=finders-vpc \
  --region=asia-northeast3 \
  --range=10.0.1.0/24

# Private Subnet
gcloud compute networks subnets create finders-private \
  --network=finders-vpc \
  --region=asia-northeast3 \
  --range=10.0.2.0/24 \
  --enable-private-ip-google-access
```

#### Cloud NAT 생성 (Private Subnet용)
```bash
# Router 생성
gcloud compute routers create finders-router \
  --network=finders-vpc \
  --region=asia-northeast3

# NAT 생성
gcloud compute routers nats create finders-nat \
  --router=finders-router \
  --region=asia-northeast3 \
  --nat-all-subnet-ip-ranges \
  --auto-allocate-nat-external-ips
```

---

## 🚨 문제 해결

### Cloudflare Tunnel 안 될 때

```bash
# 1. 데몬 상태 확인
sudo systemctl status cloudflared

# 2. 로그 확인
sudo journalctl -u cloudflared -n 50

# 3. 설정 파일 검증
cloudflared tunnel validate /etc/cloudflared/config.yml

# 4. 터널 재연결
sudo systemctl restart cloudflared

# 5. 수동으로 테스트
cloudflared tunnel run finders-tunnel
```

### 사이트 접속 안 될 때

```bash
# 1. Spring Boot 실행 확인
curl http://localhost:8080/health

# 2. Cloudflare Tunnel 확인
sudo systemctl status cloudflared

# 3. DNS 확인
dig api.finders.it.kr

# 4. Cloudflare Dashboard에서 확인
# https://dash.cloudflare.com/
# - Zero Trust > Access > Tunnels
```

### DB 연결 안 될 때

```bash
# 1. Cloud SQL 인스턴스 상태
gcloud sql instances describe finders-db

# 2. 네트워크 연결 테스트
telnet 34.64.50.136 3306

# 3. 방화벽 규칙 확인 (Cloud SQL)
gcloud sql instances describe finders-db \
  --format="value(settings.ipConfiguration.authorizedNetworks)"

# 4. Spring Boot 로그 확인
sudo journalctl -u spring-boot | grep -i "database\|connection"
```

### 포트 충돌

```bash
# 8080 포트 사용 중인 프로세스 찾기
sudo lsof -i :8080

# 또는
sudo netstat -tlnp | grep :8080

# 프로세스 종료
sudo kill -9 [PID]
```

---

## 📊 모니터링

### 실시간 로그 보기
```bash
# Cloudflare Tunnel
sudo journalctl -u cloudflared -f

# Spring Boot (systemd 사용 시)
sudo journalctl -u spring-boot -f

# nginx (사용 시)
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 리소스 사용량
```bash
# CPU/메모리 실시간 모니터링
htop

# 디스크 사용량
df -h

# 특정 프로세스 리소스
top -p $(pgrep -f spring-boot)
```

---

## 🔑 중요 파일 위치

### Cloudflare
```
설정 파일: /etc/cloudflared/config.yml
인증 파일: /root/.cloudflared/[TUNNEL-ID].json
로그: journalctl -u cloudflared
```

### Spring Boot
```
애플리케이션: /home/[USER]/app/
환경 변수: /home/[USER]/app/.env
로그: /home/[USER]/app/logs/
```

### nginx (사용 시)
```
설정 파일: /etc/nginx/nginx.conf
사이트 설정: /etc/nginx/sites-available/finders
활성화된 사이트: /etc/nginx/sites-enabled/finders
로그: /var/log/nginx/
```

---

## 📞 긴급 연락처

### 서비스 장애 시
1. Cloudflare Dashboard 확인: https://dash.cloudflare.com/
2. GCP Console 확인: https://console.cloud.google.com/
3. Health 엔드포인트 확인: https://api.finders.it.kr/health
4. 로그 확인 (위 명령어 참조)

### 외부 링크
- [Cloudflare Status](https://www.cloudflarestatus.com/)
- [GCP Status](https://status.cloud.google.com/)
- [가비아 고객센터](https://customer.gabia.com/)

---

## 💡 유용한 팁

### 터미널 단축키 설정
```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
alias cf-status='sudo systemctl status cloudflared'
alias cf-restart='sudo systemctl restart cloudflared'
alias cf-logs='sudo journalctl -u cloudflared -f'
alias app-logs='sudo journalctl -u spring-boot -f'
alias gcp-fw='gcloud compute firewall-rules list'
```

### JSON 로그 포맷팅
```bash
# 로그를 보기 좋게
sudo journalctl -u spring-boot | grep ERROR | tail -n 20 | jq .
```

### 자동 알림 설정
```bash
# Cloudflare Tunnel 중단 시 이메일 알림
# GCP Monitoring 사용 (별도 설정 필요)
```

---

## 📚 더 알아보기

- [NETWORK_SECURITY.md](./NETWORK_SECURITY.md) - 상세 가이드
- [INFRASTRUCTURE.md](./INFRASTRUCTURE.md) - 인프라 정보
- [GCP_LOGGING_GUIDE.md](./GCP_LOGGING_GUIDE.md) - 로깅 설정

---

**마지막 업데이트**: 2025-01-19
**다음 검토**: 네트워크 설정 변경 시
