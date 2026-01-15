# Sentry 에러 모니터링 가이드

애플리케이션 에러를 실시간으로 추적하고 알림 받는 방법을 안내합니다.

## Sentry란?

실시간 에러 모니터링 및 성능 추적 플랫폼입니다.

### 주요 기능
| 기능 | 설명 |
|------|------|
| **Error Tracking** | 에러 자동 수집, 그룹핑, 중복 제거 |
| **Performance** | API 응답 속도, 병목 구간 추적 |
| **Release Tracking** | 어떤 배포에서 문제가 생겼는지 추적 |
| **Alerting** | Discord/Slack/Email 실시간 알림 |

### Cloud Logging과의 차이

| 항목 | Sentry | Cloud Logging |
|------|--------|---------------|
| 용도 | 에러 추적 특화 | 전체 로그 검색/분석 |
| 에러 그룹핑 | 자동 | 수동 필터링 필요 |
| 알림 | 에러 발생 시 즉시 | 별도 설정 필요 |
| 스택트레이스 | 보기 좋게 정리됨 | 텍스트 그대로 |

> **권장**: 에러 추적은 Sentry, 상세 로그 검색은 Cloud Logging 사용

---

## Sentry 대시보드 접속

### 1. Sentry 로그인
1. [Sentry](https://sentry.io) 접속
2. 팀 계정으로 로그인 (초대 받은 이메일 확인)
3. **Finders** 프로젝트 선택

### 2. 주요 화면

#### Issues (에러 목록)
- 발생한 에러 목록
- 자동으로 그룹핑됨 (같은 에러는 하나로 묶임)
- 영향 받은 사용자 수, 발생 횟수 표시

#### Performance (성능)
- API 응답 시간 모니터링
- 느린 트랜잭션 확인
- 병목 구간 분석

#### Alerts (알림)
- 알림 규칙 설정
- Discord/Slack 연동

---

## 에러 확인 방법

### 에러 목록 보기
1. 좌측 메뉴에서 **Issues** 클릭
2. 에러 목록 확인

### 에러 상세 정보
에러 클릭 시 확인 가능한 정보:

| 항목 | 설명 |
|------|------|
| **Stack Trace** | 에러 발생 위치 (파일:라인) |
| **Request** | 요청 URL, 메서드, 파라미터 |
| **User** | 영향 받은 사용자 정보 |
| **Breadcrumbs** | 에러 발생 전 동작 순서 |
| **Tags** | 환경, 브라우저 등 메타 정보 |

### 에러 상태 관리

| 상태 | 설명 |
|------|------|
| **Unresolved** | 미해결 (기본) |
| **Resolved** | 해결됨 |
| **Ignored** | 무시 (알려진 이슈 등) |

---

## Discord 알림 설정

### 1. Discord Webhook 생성
1. Discord 서버에서 알림 받을 채널 선택
2. **채널 설정** → **연동** → **웹후크** → **새 웹후크**
3. 이름 설정 (예: `Sentry Alert`)
4. **웹후크 URL 복사**

### 2. Sentry에서 연동
1. Sentry 프로젝트 → **Settings** → **Integrations**
2. **Webhooks** 선택
3. Discord Webhook URL 입력
4. **Save** 클릭

### 3. 알림 규칙 설정
1. **Alerts** → **Create Alert Rule**
2. 조건 설정:
   - When: 새 에러 발생 시
   - Filter: `environment:prod` (프로덕션만)
   - Action: Send notification via Webhook

---

## 환경별 설정

### Local (개발)
```yaml
sentry:
  enabled: false  # 로컬에서는 비활성화
```

### Production (운영)
```yaml
sentry:
  enabled: true
  traces-sample-rate: 0.3  # 30% 샘플링
```

### 환경변수
| 변수 | 설명 | 예시 |
|------|------|------|
| `SENTRY_DSN` | Sentry 프로젝트 연결 키 | `https://xxx@o0.ingest.sentry.io/xxx` |

> **DSN 확인**: Sentry → Project Settings → Client Keys (DSN)

---

## 로컬에서 테스트 (선택)

로컬에서 Sentry를 테스트하려면:

### 1. 환경변수 설정
`.env` 파일에 추가:
```
SENTRY_DSN=your-sentry-dsn-here
```

### 2. application-local.yml 수정
```yaml
sentry:
  enabled: true  # 임시로 활성화
```

### 3. 테스트 에러 발생
```java
throw new RuntimeException("Sentry 테스트");
```

### 4. Sentry 대시보드 확인
에러가 표시되면 성공!

> **주의**: 테스트 후 `enabled: false`로 되돌리기

---

## 트러블슈팅

### 에러가 Sentry에 안 보임
1. `SENTRY_DSN` 환경변수가 설정되어 있는지 확인
2. `sentry.enabled`가 `true`인지 확인
3. 프로필이 올바른지 확인 (`prod` vs `local`)

### 너무 많은 에러 알림
1. 알림 규칙에서 필터 추가 (`level:error` 등)
2. Rate limit 설정
3. 중복 에러는 자동으로 그룹핑되므로 첫 발생만 알림 받기

### 성능 데이터가 안 보임
- `traces-sample-rate`가 0보다 큰지 확인
- 프로덕션: 0.3 (30%) 권장 (비용 절감)

---

## 설정 파일 위치

| 파일 | 설명 |
|------|------|
| `build.gradle` | Sentry 의존성 |
| `application.yml` | 공통 설정 |
| `application-local.yml` | 로컬 설정 (비활성화) |
| `application-prod.yml` | 프로덕션 설정 |

---

## 참고 링크

- [Sentry 공식 문서](https://docs.sentry.io/)
- [Sentry Spring Boot 가이드](https://docs.sentry.io/platforms/java/guides/spring-boot/)
- [Sentry Pricing](https://sentry.io/pricing/) (무료: 월 5,000 이벤트)
