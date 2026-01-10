# 사진 복원 기능 & ngrok 설정 일기

> 작성일: 2026-01-06
> AI 사진 복원 기능 개발하면서 ngrok 설정한 내용 정리!

---

## 오늘 한 일 요약

1. 사진 복원 기능 구조 정리 (Inpainting 방식)
2. 자동 마스크 생성 → 프론트에서 마스크 받는 방식으로 변경
3. 불필요한 파일 삭제 (MaskGenerator 등)
4. ngrok 설치 및 고정 도메인 설정
5. 환경변수 설정

---

## 1. 사진 복원 기능이란?

### Inpainting (내부 채우기)
```
사용자가 손상된 영역을 손으로 칠함 (마스크)
        ↓
원본 + 마스크 → Replicate AI
        ↓
AI가 마스크 영역을 자연스럽게 복원
```

### 마스크 이미지 규격
- 흰색(#FFFFFF) = 복원할 영역
- 검은색(#000000) = 그대로 유지할 영역
- 원본과 동일한 크기 (width, height)

---

## 2. 왜 ngrok이 필요해?

### 웹훅 때문!

```
1. 내 컴퓨터 → Replicate (복원 요청)
   ✅ OK! (인터넷으로 나가는 요청)

2. Replicate → 내 컴퓨터 (결과 웹훅)
   ❌ 안됨!

   Replicate가 "localhost:8080"으로 보내면
   → Replicate 자기 자신의 localhost로 감
   → 내 컴퓨터가 아님!
```

### ngrok이 해결

```
Replicate → ngrok 공개 URL → 내 localhost:8080
```

ngrok이 터널 역할을 해서 외부에서 내 로컬로 접근 가능하게 만들어줌!

---

## 3. ngrok 설치 (WSL2)

```bash
# 설치
curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null
echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list
sudo apt update && sudo apt install -y ngrok

# 인증 (https://dashboard.ngrok.com/get-started/your-authtoken)
ngrok config add-authtoken <YOUR_TOKEN>
```

---

## 4. 고정 도메인 설정

### 왜 고정 도메인?
- 무료 버전은 실행할 때마다 URL이 바뀜
- 고정 도메인 쓰면 .env 한 번만 설정하면 끝!

### 내 고정 도메인
```
https://cary-syndesmotic-confusedly.ngrok-free.dev
```

### 도메인 받는 곳
https://dashboard.ngrok.com/cloud-edge/domains (무료 계정도 1개 제공)

---

## 5. 테스트할 때 실행 방법

### 터미널 2개 필요!

```bash
# 터미널 1: Spring Boot 실행
cd /home/sweetheart/projects/finders/spring
./gradlew bootRun

# 터미널 2: ngrok 실행 (고정 도메인)
ngrok http 8080 --domain=cary-syndesmotic-confusedly.ngrok-free.dev
```

### 순서는 상관없음!
둘 다 켜져 있기만 하면 OK

### ngrok 실행 화면
```
Session Status    online
Account           finders.official.kr@gmail.com
Forwarding        https://cary-syndesmotic-confusedly.ngrok-free.dev -> http://localhost:8080
```

---

## 6. 환경변수 (.env)

```bash
# Replicate AI (사진 복원)
REPLICATE_API_KEY=r8_xxxxxxxxxxxxx
REPLICATE_MODEL_VERSION=c11bac58203367db93a3c552bd49a25a5418458ddffb7e90dae55780765e26d6
WEBHOOK_BASE_URL=https://cary-syndesmotic-confusedly.ngrok-free.dev/api
```

### REPLICATE_MODEL_VERSION
Inpainting 모델 버전 (해시 값)

**현재 사용 중**: `stability-ai/stable-diffusion-inpainting`
- 모델 페이지: https://replicate.com/stability-ai/stable-diffusion-inpainting
- 최신 버전 확인: 페이지에서 "Latest version" 클릭 → 해시 값 복사

**다른 Inpainting 모델 옵션**:
1. **stability-ai/stable-diffusion-inpainting** (현재 사용 중) ⭐ 추천
   - 사진 복원에 최적화
   - 안정적이고 검증된 모델
   
2. **runwayml/stable-diffusion-inpainting**
   - RunwayML 기반, 고품질 결과
   - 모델 페이지: https://replicate.com/runwayml/stable-diffusion-inpainting

3. **lkwq007/stablediffusion-inpainting**
   - 커뮤니티 모델
   - 모델 페이지: https://replicate.com/lkwq007/stablediffusion-inpainting

**모델 변경 시 주의사항**:
- 각 모델마다 입력 파라미터가 다를 수 있음
- `ReplicateRequest.Input` 클래스 확인 필요
- 모델 페이지에서 API 스펙 확인 후 테스트 권장

---

## 7. API 테스트

### Swagger
```
http://localhost:8080/api/swagger-ui.html
```

### 복원 요청
```
POST /restorations
Content-Type: multipart/form-data

- originalImage: 원본 사진 (JPG/PNG)
- maskImage: 마스크 이미지 (PNG)
```

### 결과 조회 (폴링)
```
GET /restorations/{id}

status: PENDING → PROCESSING → COMPLETED
```

---

## 8. 주의사항

### ngrok 관련
- **동시에 한 명만** 같은 도메인 사용 가능
- ngrok 끄면 웹훅 못 받음
- 테스트할 때마다 ngrok 실행 필요

### Replicate 관련
- **유료 서비스**: 무료 크레딧 있지만 소진되면 과금
- 처리 시간: 보통 10~30초, 첫 요청은 1~2분 걸릴 수 있음 (cold start)

### 마스크 이미지
- 프론트에서 Canvas로 생성해서 전송
- 테스트용으로 간단한 흰/검 PNG 만들어서 사용 가능

---

## 9. 변경된 파일 목록

### 삭제된 파일
```
infra/image/
├── MaskGenerator.java           (삭제)
├── OverexposureMaskGenerator.java (삭제)
├── ImageProperties.java         (삭제)
└── ImageConfig.java             (삭제)
```

### 수정된 파일
- `PhotoRestoration.java` - mask_data 제거, mask_url 유지
- `RestorationRequest.java` - maskImage 필드 추가
- `PhotoRestorationService.java` - 자동 마스크 생성 로직 제거
- `StoragePath.java` - RESTORATION_MASK 추가
- `ERD.md` - mask_data → mask_url 변경

---

## 10. 로컬 테스트 삽질 기록 (01/06 새벽)

### 첫 번째 문제: 회원이 없음
```
POST /api/restorations → 404
{
  "code": "MEMBER_404",
  "message": "회원을 찾을 수 없습니다."
}
```

**원인**: 컨트롤러에서 `TEMP_MEMBER_ID = 1L` 하드코딩인데 DB에 회원이 없었음

**해결**: MySQL에 테스트 회원 직접 삽입
```sql
-- 테스트 회원 생성 (member + member_user 둘 다 필요!)
INSERT INTO member (dtype, name, email, phone, status, created_at, updated_at)
VALUES ('USER', '테스트유저', 'test@finders.com', '010-1234-5678', 'ACTIVE', NOW(), NOW());

SET @member_id = LAST_INSERT_ID();

INSERT INTO member_user (member_id, nickname, token_balance)
VALUES (@member_id, 'testuser', 3);
```

실행 명령어:
```bash
docker exec finders-mysql mysql -ufinders -pfinders123 finders -e "위 SQL"
```

### 두 번째 문제: Signed URL 생성 실패
```
java.lang.IllegalStateException: Signing key was not provided and could not be derived
```

**진행 상황**:
- ✅ 회원 조회 - 성공
- ✅ GCS 업로드 - 성공 (원본 + 마스크)
- ✅ 토큰 차감 - 성공 (3 → 2)
- ✅ photo_restoration INSERT - 성공
- ❌ **Signed URL 생성 - 실패**
- ❌ Replicate API 호출 - 못함

**원인**:
- 로컬에서 `gcloud auth application-default login`으로 인증함
- ADC(Application Default Credentials)는 **사용자 계정** 자격 증명
- Signed URL 생성에는 **서비스 계정 Private Key**가 필요
- Google이 서비스 계정 키 파일(.json) 사용 비권장해서 ADC 쓰는데, ADC는 Signed URL 지원 안 함

**결론**:
> 🚫 **로컬에서 사진 복원 API 전체 테스트 불가능**
>
> 배포 환경(Cloud Run)에서는 서비스 계정이 자동으로 붙어서 Signed URL 생성 가능.
> **배포 후 테스트 필수!**

### 로컬 테스트 가능/불가능 정리

| 기능 | 로컬 (ADC) | 배포 (Cloud Run) |
|------|-----------|------------------|
| 회원 조회 | ✅ | ✅ |
| GCS 업로드 | ✅ | ✅ |
| 토큰 차감 | ✅ | ✅ |
| DB 저장 | ✅ | ✅ |
| Public URL | ✅ | ✅ |
| **Signed URL** | ❌ | ✅ |
| **Replicate 호출** | ❌ | ✅ |

---

## 11. 코드 리팩토링 (01/06 오후)

### 토큰 차감 시점 변경

**Before** (문제 있었음):
```
1. 복원 요청 시 토큰 차감
2. Replicate API 실패 시 토큰 환불
   → 트랜잭션 문제로 환불 안 될 수 있음!
```

**After** (수정됨):
```
1. 복원 요청 시 토큰 잔액 확인만 (차감 X)
2. 복원 완료 시 토큰 차감
3. 실패 시 차감 안 했으니 환불 불필요
```

### 결과 이미지 GCS 저장 구현

기존에 TODO로 남겨뒀던 `downloadAndStoreResult` 구현!

```
Replicate 임시 URL (만료됨)
        ↓
WebClient로 다운로드
        ↓
ByteArrayMultipartFile 변환
        ↓
GCS에 영구 저장
        ↓
restorations/{memberId}/restored/{uuid}.png
```

### 트랜잭션 분리 및 중복 방지

1. **중복 webhook 방지**: `completeRestoration`, `failRestoration` 시작 시 이미 처리된 건 스킵
2. **트랜잭션 분리**: `completeRestoration` 실패 시 `WebhookController`에서 catch해서 `failRestoration` 호출 (별도 트랜잭션)

### 수정된 파일
- `PhotoRestorationService.java` - 토큰 차감 시점 변경, 결과 이미지 저장 구현, 중복 방지
- `ReplicateWebhookController.java` - 예외 처리 개선
- `ERD.md` - GCS 경로에 mask_url 추가, DDL에 replicate_prediction_id/error_message 추가

---

## 12. 다음에 할 것

- [x] REPLICATE_MODEL_VERSION 값 설정
- [x] 테스트 회원 생성
- [x] 토큰 차감 시점 변경 (요청 시 → 완료 시)
- [x] 결과 이미지 GCS 저장 구현
- [ ] **배포 후 실제 테스트** ← 로컬 불가!
- [ ] 프론트엔드와 API 연동 테스트
- [ ] 웹훅 컨트롤러 동작 확인

---

## 유용한 명령어 모음

```bash
# ngrok 상태 확인
curl http://localhost:4040/api/tunnels

# Spring Boot 실행
./gradlew bootRun

# 빌드만
./gradlew build -x test

# ngrok 실행 (고정 도메인)
ngrok http 8080 --domain=cary-syndesmotic-confusedly.ngrok-free.dev
```

---

## 참고 링크

- [Replicate](https://replicate.com)
- [Replicate API 토큰](https://replicate.com/account/api-tokens)
- [Stable Diffusion Inpainting](https://replicate.com/stability-ai/stable-diffusion-inpainting)
- [ngrok 대시보드](https://dashboard.ngrok.com)
- [ngrok 고정 도메인](https://dashboard.ngrok.com/cloud-edge/domains)
