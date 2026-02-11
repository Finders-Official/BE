# Architecture

## Overview

Finders API는 **도메인 기반 계층형 아키텍처 (Package by Feature + Layered Architecture)**를 사용합니다.

> **참고**: 이 아키텍처는 DDD의 일부 전술적 패턴(Entity 비즈니스 메서드, 팩토리 메서드 등)을 적용하지만,
> 순수 DDD(Aggregate, Value Object, Domain Event, Bounded Context 등)와는 다릅니다.

## Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                      Controller Layer                        │
│                    (API 진입점, 요청/응답)                      │
├─────────────────────────────────────────────────────────────┤
│                       Service Layer                          │
│                    (비즈니스 로직 처리)                         │
├─────────────────────────────────────────────────────────────┤
│                      Repository Layer                        │
│                    (데이터 접근, QueryDSL)                     │
├─────────────────────────────────────────────────────────────┤
│                       Entity Layer                           │
│                      (도메인 모델)                             │
└─────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.finders.api/
│
├── domain/                 # 도메인 레이어 (핵심 비즈니스)
│   └── {domain}/
│       ├── controller/     # REST API 컨트롤러
│       ├── service/        # 비즈니스 로직
│       ├── repository/     # 데이터 접근
│       ├── entity/         # JPA 엔티티
│       ├── dto/            # 요청/응답 DTO
│       └── enums/          # 도메인 상수
│
├── infra/                  # 인프라 레이어 (외부 시스템, 교체 가능)
│   ├── storage/            # 파일 저장소 (GCS)
│   ├── oauth/              # OAuth 클라이언트 (Kakao, Apple)
│   ├── payment/            # 결제 (토스 페이먼츠)
│   └── replicate/          # AI 이미지 복원 (Replicate SUPIR)
│
└── global/                 # 글로벌 레이어 (공통 모듈)
    ├── config/             # 설정 클래스
    ├── entity/             # Base Entity
    ├── response/           # 통일된 응답 구조
    └── exception/          # 전역 예외 처리
```

## Domain Structure

### 1. Member (회원)
- 회원 가입, 탈퇴, 프로필 관리
- 배송지 관리

### 2. Auth (인증)
- 소셜 로그인 (Kakao, Apple)
- JWT 토큰 발급/갱신

### 3. Store (현상소)
- 현상소 정보 관리
- 검색, 필터링, 랭킹

### 4. Reservation (예약)
- 현상 예약 관리
- 시간 슬롯, 작업 옵션

### 5. Photo (사진)
- 스캔된 사진 관리
- AI 복원 기능 (Replicate - SUPIR 초해상도 복원)
- 인화 주문

### 6. Community (커뮤니티)
- 피드 게시글
- 좋아요, 댓글

### 7. Inquiry (문의)
- 1:1 문의 관리

## Design Patterns

### 1. Strategy Pattern
OAuth 로그인 방식을 유연하게 교체 가능하도록 설계

```java
public interface OAuthClient {
    OAuthUserInfo getUserInfo(String code);
}

@Component
public class KakaoOAuthClient implements OAuthClient { ... }

@Component
public class AppleOAuthClient implements OAuthClient { ... }
```

### 2. Adapter Pattern
외부 API를 내부 인터페이스로 추상화

```java
// Storage 예시 - GCS를 S3로 교체 가능
public interface StorageService {
    StorageResponse.Upload uploadPublic(MultipartFile file, StoragePath path, Object... args);
}

@Service
public class GcsStorageService implements StorageService { ... }
// 필요시: public class S3StorageService implements StorageService { ... }
```

### 3. Observer Pattern (Spring Event)
도메인 이벤트를 통한 느슨한 결합

```java
// 이벤트 발행
eventPublisher.publishEvent(new ReservationCreatedEvent(reservation));

// 이벤트 구독
@EventListener
public void handleReservationCreated(ReservationCreatedEvent event) { ... }
```

## External Services

| Service | Purpose | Package |
|---------|---------|---------|
| Google Cloud Storage | 파일 저장소 | `infra.storage` |
| Kakao OAuth | 소셜 로그인 | `infra.oauth.kakao` |
| Apple OAuth | 소셜 로그인 | `infra.oauth.apple` |
| 토스 페이먼츠 | 결제 | `infra.payment` |
| Replicate (SUPIR) | AI 이미지 복원 | `infra.replicate` |

## Database Design

### Base Entity
모든 엔티티는 `BaseEntity` 또는 `BaseTimeEntity`를 상속

```java
// 생성일/수정일만 필요한 경우
public class SomeEntity extends BaseTimeEntity { }

// Soft Delete가 필요한 경우
public class SomeEntity extends BaseEntity { }
```

### Auditing
- `@CreatedDate`: 생성 시점 자동 기록
- `@LastModifiedDate`: 수정 시점 자동 기록
- `deletedAt`: Soft Delete 처리
