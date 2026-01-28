# 원격 DB 접속 가이드

> Cloud SQL(finders-db)에 로컬 IDE에서 접속하는 방법

## 개요

Cloud SQL의 공개 IP가 비활성화되어 있어 **IAP 터널**을 통해서만 접속 가능합니다.

```
[로컬 PC] → [IAP 터널] → [GCE 서버] → [Cloud SQL]
localhost:3307 → 암호화된 터널 → 10.0.2.2 → 10.68.240.3:3306
```

## 사전 준비

### 1. gcloud CLI 설치

아직 설치하지 않았다면 [Google Cloud SDK](https://cloud.google.com/sdk/docs/install)를 설치하세요.

```bash
# 설치 확인
gcloud --version
```

### 2. gcloud 로그인

```bash
# Google 계정으로 로그인 (브라우저 열림)
gcloud auth login

# Application Default Credentials 설정
gcloud auth application-default login

# 프로젝트 설정
gcloud config set project project-37afc2aa-d3d3-4a1a-8cd
```

### 3. IAM 권한 확인

IAP 터널 사용을 위해 다음 권한이 필요합니다:
- `roles/iap.tunnelResourceAccessor` (IAP 터널 접근)
- `roles/compute.viewer` (Compute Engine 조회)

> 권한이 없다면 프로젝트 관리자에게 요청하세요.

---

## 접속 방법

### Step 1: IAP 터널 열기

**터미널을 열고** 아래 명령어를 실행합니다. (접속 중에는 터미널을 닫지 마세요!)

```bash
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap \
  -- -L 3307:10.68.240.3:3306
```

> **보안 안내**: 위 명령어에 포함된 정보(Project ID, Private IP 등)는 공개되어도 보안 위협이 없습니다.
> - IAP 인증: GCP IAM 권한이 없으면 터널 자체가 열리지 않음
> - Private IP: VPC 내부 IP라 외부에서 직접 접근 불가능
> - 실제 민감 정보(비밀번호 등)는 이 문서에 포함되어 있지 않습니다.

성공하면 아래와 같은 메시지가 출력됩니다:
```
Warning: Permanently added 'compute.xxxxx' (ED25519) to the list of known hosts.
Welcome to Ubuntu 22.04...
```

> **주의**: 이 터미널 창은 DB 작업이 끝날 때까지 열어두세요!

---

### Step 2: DB 클라이언트에서 연결

터널이 열린 상태에서 **새 터미널** 또는 **IDE**에서 DB에 연결합니다.

#### 연결 정보

| 항목 | 값 |
|------|-----|
| Host | `localhost` |
| Port | `3307` |
| User | `finders` |
| Password | `.env.dev` 또는 `.env.prod` 참조 |
| Database | `finders_dev` (개발) / `finders` (운영) |

---

## IDE별 설정 방법

### IntelliJ IDEA (Database Tool)

1. **View > Tool Windows > Database** 또는 우측 사이드바 **Database** 클릭
2. **+ > Data Source > MySQL** 선택
3. 연결 정보 입력:
   - Host: `localhost`
   - Port: `3307`
   - User: `finders`
   - Password: (팀 공유 문서 참조)
   - Database: `finders_dev`
4. **Test Connection** 클릭하여 연결 확인
5. **OK** 클릭

### DataGrip

1. **File > New > Data Source > MySQL**
2. 연결 정보 입력 (IntelliJ와 동일)
3. **Test Connection** → **OK**

### VS Code (SQLTools - 무료, 추천)

> **주의**: Database Client(cweijan)는 3개 연결 초과 시 유료입니다. **SQLTools**(mtxr)를 추천합니다!

**설치:**
1. Extensions에서 **SQLTools** (mtxr) 설치
2. Extensions에서 **SQLTools MySQL/MariaDB** 드라이버 설치

**연결 설정:**
1. 좌측 사이드바 **SQLTools 아이콘** 클릭
2. **Add New Connection** 클릭
3. **MySQL** 선택
4. 연결 정보 입력:
   - Connection Name: `Finders Dev`
   - Server: `localhost`
   - Port: `3307`
   - Database: `finders_dev`
   - Username: `finders`
   - Password: (팀 공유 문서 참조)
5. **Test Connection** → **Save Connection**

### MySQL Workbench

1. **MySQL Connections** 옆 **+** 클릭
2. 연결 정보 입력:
   - Connection Name: `Finders Dev (IAP Tunnel)`
   - Hostname: `localhost`
   - Port: `3307`
   - Username: `finders`
3. **Test Connection** → 비밀번호 입력 → **OK**

### 터미널 (mysql CLI)

```bash
mysql -h localhost -P 3307 -u finders -p finders_dev
```

---

## 환경별 Database

| 환경 | Database | 용도 |
|------|----------|------|
| `finders_dev` | 개발 DB | FE 연동 테스트, Mock 데이터 |
| `finders` | 운영 DB | 실제 서비스 데이터 (주의!) |

> **주의**: 운영 DB(`finders`)에서 DELETE/UPDATE 작업 시 각별히 주의하세요!

---

## 문제 해결

### "Permission denied" 오류

```
ERROR: (gcloud.compute.ssh) Could not fetch resource
```

**해결**: IAM 권한이 없습니다. 프로젝트 관리자에게 `roles/iap.tunnelResourceAccessor` 권한을 요청하세요.

### "Connection refused" 오류

```
ERROR 2003 (HY000): Can't connect to MySQL server on 'localhost:3307'
```

**해결**: 
1. IAP 터널이 열려있는지 확인 (Step 1의 터미널이 실행 중인지)
2. 포트가 맞는지 확인 (`3307`)

### "Access denied for user" 오류

```
ERROR 1045 (28000): Access denied for user 'finders'@'xxx'
```

**해결**: 비밀번호가 틀립니다. 팀 공유 문서에서 최신 비밀번호를 확인하세요.

### 터널 연결이 끊어짐

장시간 미사용 시 터널이 자동으로 끊어질 수 있습니다. Step 1을 다시 실행하세요.

---

## 팁

### 터널 백그라운드 실행 (선택)

터미널 창을 열어두기 싫다면 백그라운드로 실행:

```bash
gcloud compute ssh finders-server-v2 \
  --zone=asia-northeast3-a \
  --project=project-37afc2aa-d3d3-4a1a-8cd \
  --tunnel-through-iap \
  -- -L 3307:10.68.240.3:3306 -N -f
```

종료하려면:
```bash
# 프로세스 찾기
ps aux | grep "ssh.*3307"

# 종료
kill [PID]
```

### alias 설정 (선택)

자주 사용한다면 `~/.bashrc` 또는 `~/.zshrc`에 추가:

```bash
alias finders-db='gcloud compute ssh finders-server-v2 --zone=asia-northeast3-a --project=project-37afc2aa-d3d3-4a1a-8cd --tunnel-through-iap -- -L 3307:10.68.240.3:3306'
```

이후 `finders-db` 명령어로 간단히 터널 열기!

---

## 참고 문서

- [INFRASTRUCTURE.md](../architecture/INFRASTRUCTURE.md) - 인프라 정보
- [NETWORK_SECURITY.md](../infra/NETWORK_SECURITY.md) - 네트워크 보안 설정
- [LOCAL_DEVELOPMENT.md](./LOCAL_DEVELOPMENT.md) - 로컬 개발 환경

---

**마지막 업데이트**: 2025-01-27
