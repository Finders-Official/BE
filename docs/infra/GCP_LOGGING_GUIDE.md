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
- **Cloud Logging Logback Appender** 사용
- **Structured JSON** 형식으로 전송 (로그 레벨 → Severity 자동 매핑)
- **logback-spring.xml** 설정 기반

### 기본 필터 사용

#### 애플리케이션 로그 조회
```
resource.type="global"
logName="projects/project-37afc2aa-d3d3-4a1a-8cd/logs/java.log"
```

#### 에러 로그만 조회
```
resource.type="global"
logName=~"java.log"
severity>=ERROR
```

#### 특정 클래스 로그 조회
```
resource.type="global"
logName=~"java.log"
jsonPayload.logger_name=~"com.finders.api"
```

#### 특정 시간대 로그 조회
- 우측 상단의 시간 범위 선택기 사용
- 기본값: 최근 1시간

### 자주 쓰는 필터

| 용도 | 필터 |
|------|------|
| 전체 앱 로그 | `logName=~"java.log"` |
| ERROR 이상 | `logName=~"java.log" severity>=ERROR` |
| 특정 키워드 | `logName=~"java.log" jsonPayload.message=~"키워드"` |
| 특정 로거 | `logName=~"java.log" jsonPayload.logger_name=~"GcsStorageService"` |

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

## SSH 접속 (대안)

Cloud Logging이 기본이지만, Docker 로그를 직접 확인할 수도 있습니다.

### 접속 방법
1. **Compute Engine** → **VM 인스턴스** 이동
2. 해당 인스턴스의 **SSH** 버튼 클릭
3. 브라우저에서 터미널 열림

### Docker 로그 확인
```bash
# 실시간 로그 (실행 중인 컨테이너)
docker logs -f finders-api

# 최근 100줄
docker logs --tail 100 finders-api

# 에러만 필터링
docker logs finders-api 2>&1 | grep -i error

# 타임스탬프 포함
docker logs -t finders-api
```

> **참고**: 컨테이너 재시작 시 이전 로그가 사라질 수 있습니다. 영구 보관이 필요하면 Cloud Logging을 사용하세요.

## 문제 해결

### "권한이 없습니다" 오류
- 올바른 Google 계정으로 로그인했는지 확인
- 프로젝트 관리자에게 권한 요청

### 로그가 보이지 않음
- 시간 범위 설정 확인
- 필터 조건 확인 (너무 좁은 조건)
- 프로젝트가 올바르게 선택되었는지 확인

## 참고 링크

- [GCP Cloud Logging 문서](https://cloud.google.com/logging/docs)
- [로그 쿼리 언어 가이드](https://cloud.google.com/logging/docs/view/logging-query-language)
- [Cloud Monitoring 문서](https://cloud.google.com/monitoring/docs)
