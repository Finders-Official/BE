# GCP 로그 확인 가이드

팀원들이 서버 로그를 확인하는 방법을 안내합니다.

## 사전 준비

### 권한 확인
로그를 확인하려면 다음 권한이 필요합니다:
- `roles/logging.viewer` - 로그 뷰어
- `roles/monitoring.viewer` - 모니터링 뷰어

> 권한이 없다면 프로젝트 관리자에게 요청하세요.

## GCP 콘솔 접속

### 1. GCP 콘솔 로그인
1. [Google Cloud Console](https://console.cloud.google.com) 접속
2. Google 계정으로 로그인 (권한이 부여된 계정)
3. 상단의 프로젝트 선택 드롭다운에서 **Finders 프로젝트** 선택

### 2. 로그 탐색기 접속
1. 좌측 메뉴에서 **Logging** → **로그 탐색기** 클릭
2. 또는 직접 접속: [로그 탐색기](https://console.cloud.google.com/logs/query)

## 로그 조회 방법

### 로그 구조
- **Docker `gcplogs` 드라이버**를 통해 Cloud Logging으로 전송 (prod 환경만 해당)
- **dev 환경**은 `json-file` 드라이버를 사용하여 로컬 저장 (Cloud Logging 비용 방지)
- **logback-spring.xml** 설정 기반으로 로그 레벨 제어

### 기본 필터 사용

#### 애플리케이션 로그 조회
```
resource.type="gce_instance"
logName="projects/finders-487717/logs/gcplogs"
```

#### 에러 로그만 조회
```
resource.type="gce_instance"
logName=~"gcplogs"
severity>=ERROR
```

#### 특정 클래스 로그 조회
```
resource.type="gce_instance"
logName=~"gcplogs"
jsonPayload.logger_name=~"com.finders.api"
```

#### 특정 시간대 로그 조회
- 우측 상단의 시간 범위 선택기 사용
- 기본값: 최근 1시간

### 자주 쓰는 필터

| 용도 | 필터 |
|------|------|
| 전체 앱 로그 | `logName=~"gcplogs"` |
| ERROR 이상 | `logName=~"gcplogs" severity>=ERROR` |
| 특정 키워드 | `logName=~"gcplogs" jsonPayload.message=~"키워드"` |
| 특정 로거 | `logName=~"gcplogs" jsonPayload.logger_name=~"GcsStorageService"` |

### Severity 레벨 매핑

| Java Level | Cloud Logging Severity |
|------------|------------------------|
| TRACE | DEBUG |
| DEBUG | DEBUG |
| INFO | INFO |
| WARN | WARNING |
| ERROR | ERROR |

### 필터 저장하기
1. 필터 입력 후 **저장** 버튼 클릭
2. 이름 입력 후 저장
3. 다음에 저장된 필터 불러오기 가능

## 모니터링 대시보드

### 대시보드 접속
1. 좌측 메뉴에서 **Monitoring** → **대시보드** 클릭
2. 또는 직접 접속: [모니터링 대시보드](https://console.cloud.google.com/monitoring/dashboards)

### 확인 가능한 지표
- CPU 사용률
- 메모리 사용량
- 네트워크 트래픽
- 디스크 I/O

### 환경별 로깅 방식

| 환경 | Docker 로그 드라이버 | 로그 확인 방법 |
|------|---------------------|----------------|
| **Prod** | `gcplogs` → Cloud Logging | GCP 로그 탐색기 (아래 필터 사용) |
| **Dev** | `json-file` (로컬 저장) | `docker logs -f <컨테이너>` |
| **Local** | Docker 기본 (stdout) | 터미널 콘솔 출력 |

- **Prod**: `docker logs -f` 명령어가 동작하지 않습니다. 반드시 GCP 로그 탐색기를 이용해 주세요.
- **Dev**: Cloud Logging으로 전송되지 않습니다. SSH 접속 후 `docker logs -f`로 확인하세요.

## 참고 링크

- [GCP Cloud Logging 문서](https://cloud.google.com/logging/docs)
- [로그 쿼리 언어 가이드](https://cloud.google.com/logging/docs/view/logging-query-language)
- [Cloud Monitoring 문서](https://cloud.google.com/monitoring/docs)
