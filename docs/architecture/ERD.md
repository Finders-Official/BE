# Finders ERD

> 필름 현상소 예약 서비스 데이터베이스 설계서
> v3.0.0 | 2026-01-18

---

## 테이블 목록 (36개)

| 도메인             | 테이블                       | 설명                           |
|-----------------|---------------------------|------------------------------|
| **member**      | `member`                  | 회원 Base (Joined Table 상속)    |
|                 | `member_user`             | User 전용 (소셜 로그인 사용자, 토큰 관련)  |
|                 | `member_owner`            | Owner 전용 (현상소 사장님, 정산 계좌)    |
|                 | `member_admin`            | Admin 전용 (관리자, 추후 확장)        |
|                 | `social_account`          | 소셜 로그인 연동 - User 전용 (카카오/애플) |
|                 | `member_address`          | 배송지 주소 - User 전용             |
|                 | `member_device`           | FCM 디바이스 토큰 (푸시 알림용)         |
|                 | `member_agreement`        | 약관 동의 이력                     |
|                 | `terms`                   | 약관 버전 관리                     |
|                 | `token_history`           | AI 토큰 충전/사용 내역 - User 전용     |
|                 | `favorite_photo_lab`      | 관심 현상소                       |
| **store**       | `photo_lab`               | 현상소 정보                       |
|                 | `photo_lab_image`         | 현상소 이미지                      |
|                 | `photo_lab_tag`           | 현상소-태그 조인 테이블                |
|                 | `tag`                     | 현상소 태그                       |
|                 | `photo_lab_notice`        | 현상소 공지사항                     |
|                 | `photo_lab_business_hour` | 영업시간                         |
|                 | `photo_lab_document`      | 사업자 증빙 서류                    |
|                 | `region`                  | 지역 (시/도, 시/군/구)              |
| **reservation** | `reservation`             | 예약 정보                        |
| **photo**       | `development_order`       | 현상 주문                        |
|                 | `scanned_photo`           | 스캔된 사진                       |
|                 | `print_order`             | 인화 주문                        |
|                 | `print_order_item`        | 인화 주문 상세                     |
|                 | `print_order_photo`       | 인화 대상 사진 + 수량 (주문-스캔사진 매핑)   |
|                 | `delivery`                | 배송 정보                        |
|                 | `photo_restoration`       | AI 사진 복원 요청                  |
| **community**   | `post`                    | 게시글/리뷰 (자가현상 여부 포함)          |
|                 | `post_image`              | 게시글 이미지                      |
|                 | `comments`                | 댓글                           |
|                 | `post_like`               | 좋아요                          |
| **inquiry**     | `inquiry`                 | 1:1 문의                       |
|                 | `inquiry_image`           | 문의 첨부 이미지                    |
|                 | `inquiry_reply`           | 문의 답변                        |
| **common**      | `notice`                  | 공지사항                         |
|                 | `promotion`               | 프로모션/배너                      |
|                 | `notification`            | 알림                           |
|                 | `payment`                 | 결제 정보 (포트원 V2)               |

---

## ERD 관계도

```
member (Base: Joined Table 상속)
   │
   ├── 1:1 ─ member_user ─┬─ 1:N ─ social_account (소셜 로그인)
   │                      ├─ 1:N ─ member_address (배송지)
   │                      └─ 1:N ─ token_history (토큰 내역)
   │
   ├── 1:1 ─ member_owner ─── 1:N ─ photo_lab (현상소 운영)
   │
   └── 1:1 ─ member_admin (추후 확장)

member ─┬─ 1:N ─ member_device (FCM 토큰, 공통)
        ├─ 1:N ─ member_agreement ─── N:1 ─ terms (약관 버전, 공통)
        ├─ 1:N ─ reservation ──── 0..1:1 ─ development_order ─┬─ 1:N ─ scanned_photo
        │                                                     └─ 0..1:N ─ print_order ─┬─ 1:N ─ print_order_item
        │                                                                              └─ 0..1:1 ─ delivery
        ├─ 1:N ─ development_order (현장 주문, 예약 없이)
        ├─ 1:N ─ print_order (현장 주문, 현상 없이)
        ├─ 1:N ─ photo_restoration (AI 복원, 토큰 사용)
        ├─ 1:N ─ post ─┬─ 1:N ─ post_image
        │              ├─ 1:N ─ comments
        │              └─ 1:N ─ post_like
        ├─ 1:N ─ favorite_photo_lab
        ├─ 1:N ─ inquiry ─┬─ 1:N ─ inquiry_image
        │                 └─ 1:N ─ inquiry_reply
        ├─ 1:N ─ notification
        └─ 1:N ─ payment (결제, 토큰 구매)

photo_lab ─┬─ 1:N ─ photo_lab_image
           ├─ 1:1 ─ photo_lab_tag ─── 1:N ─ tag (현상소 태그)
           ├─ 1:N ─ photo_lab_notice
           ├─ 1:N ─ photo_lab_business_hour
           ├─ 1:N ─ photo_lab_document (증빙서류)
           ├─ 1:N ─ reservation
           ├─ 1:N ─ development_order (현장 주문)
           └─ 1:N ─ print_order (현장 주문)
```

---

## Enum 정의

```java
// 회원 (Joined Table 상속)
MemberType:USER,OWNER,ADMIN  // role 컬럼 (discriminator)
MemberStatus:ACTIVE,SUSPENDED,WITHDRAWN
SocialProvider:KAKAO,APPLE    // User 전용
DeviceType:IOS,ANDROID,WEB

// 현상소
PhotoLabStatus:PENDING,ACTIVE,SUSPENDED,CLOSED

// 예약/주문
ReservationStatus:RESERVED,COMPLETED,CANCELLED
DevelopmentOrderStatus:RECEIVED,DEVELOPING,SCANNING,COMPLETED
PrintOrderStatus:PENDING,CONFIRMED,PRINTING,READY,SHIPPED,COMPLETED
ReceiptMethod:PICKUP,DELIVERY
DeliveryStatus:PENDING,PREPARING,SHIPPED,IN_TRANSIT,DELIVERED

// 커뮤니티
PostStatus:ACTIVE,HIDDEN,DELETED

// 문의
InquiryStatus:PENDING,ANSWERED,CLOSED

// 공지/알림
NoticeType:GENERAL,EVENT,POLICY
NotificationType:ORDER,RESERVATION,COMMUNITY,MARKETING,NOTICE
PromotionType:BANNER,POPUP,EVENT

// 인화 옵션
PaperType:GLOSSY,MATTE,SILK,LUSTER
PrintMethod:INKJET,LASER,SILVER_HALIDE
PrintProcess:NORMAL,BORDER,BORDERLESS

// 약관/결제/복원 (v1.2.0 추가)
AgreementType:TERMS,PRIVACY,MARKETING,LOCATION
DocumentType:BUSINESS_LICENSE,BUSINESS_PERMIT
RestorationStatus:PENDING,PROCESSING,COMPLETED,FAILED
FeedbackRating:GOOD,BAD

// 결제/토큰 (v2.4.0 포트원 V2 전환)
PaymentStatus:          // 포트원 V2 표준
READY                 // 결제 대기
        PENDING               // 결제 진행 중
VIRTUAL_ACCOUNT_ISSUED // 가상계좌 발급됨
        PAID                  // 결제 완료
FAILED                // 결제 실패
        PARTIAL_CANCELLED     // 부분 취소
CANCELLED             // 전액 취소

PaymentMethod:          // 포트원 V2 결제수단 (사용: 카드, 계좌이체, 가상계좌, 간편결제)
  CARD, TRANSFER, VIRTUAL_ACCOUNT, EASY_PAY

PgProvider:             // PG사/간편결제 제공자 (현재: KCP + 간편결제 3종)
  KCP,                  // 메인 PG사 (카드, 계좌이체, 가상계좌)
  KAKAOPAY, NAVERPAY, TOSSPAY  // 간편결제
  // 확장 가능: TOSSPAYMENTS, INICIS, NICE 등

OrderType:TOKEN_PURCHASE,DEVELOPMENT_ORDER,PRINT_ORDER
TokenHistoryType:SIGNUP_BONUS,REFRESH,PURCHASE,USE,REFUND
```

---

## GCS (Cloud Storage) 규칙

> 이미지/파일 저장소 규칙. DB에는 `object_path`(경로)만 저장하고, API 응답 시 URL 변환.

### 버킷 구성

| 버킷                | 용도                        | 접근 방식            |
|-------------------|---------------------------|------------------|
| `finders-public`  | 공개 이미지 (현상소, 게시글, 프로필 등)  | 직접 URL           |
| `finders-private` | 비공개 파일 (스캔 사진, 서류, AI 복원) | Signed URL (1시간) |

### 경로 규칙

| 테이블                  | 컬럼              | 버킷          | 경로 패턴                                                           |
|----------------------|-----------------|-------------|-----------------------------------------------------------------|
| `member`             | `profile_image` | public      | `profiles/{memberId}/{uuid}.{ext}`                              |
| `photo_lab_image`    | `object_path`   | public      | `photo-labs/{photoLabId}/images/{uuid}.{ext}`                   |
| `photo_lab`          | `qr_code_url`   | public      | `photo-labs/{photoLabId}/qr.png`                                |
| `photo_lab_document` | `object_path`   | **private** | `photo-labs/{photoLabId}/documents/{documentType}/{uuid}.{ext}` |
| `photo_lab_notice`   | -               | -           | 이미지 없음 (텍스트만)                                                   |
| `scanned_photo`      | `object_path`   | **private** | `temp/orders/{developmentOrderId}/scans/{uuid}.{ext}`           |
| `post_image`         | `object_path`   | public      | `posts/{postId}/{uuid}.{ext}`                                   |
| `inquiry_image`      | `object_path`   | public      | `inquiries/{inquiryId}/{uuid}.{ext}`                            |
| `photo_restoration`  | `original_path` | **private** | `restorations/{memberId}/original/{uuid}.{ext}`                 |
| `photo_restoration`  | `mask_path`     | **private** | `restorations/{memberId}/mask/{uuid}.{ext}`                     |
| `photo_restoration`  | `restored_path` | **private** | `restorations/{memberId}/restored/{uuid}.{ext}`                 |
| `promotion`          | `object_path`   | public      | `promotions/{promotionId}/{uuid}.{ext}`                         |

### 자동 삭제 (Lifecycle)

`temp/` prefix가 붙은 모든 파일은 **30일 후 자동 삭제** (GCS Lifecycle 정책)

| 용도     | 버킷      | 경로 패턴                                                 |
|--------|---------|-------------------------------------------------------|
| 임시 업로드 | public  | `temp/{memberId}/{uuid}.{ext}`                        |
| 스캔 사진  | private | `temp/orders/{developmentOrderId}/scans/{uuid}.{ext}` |

- 임시 업로드: 엔티티 연결 시 영구 경로로 이동
- 스캔 사진: 고객이 30일 내 다운로드 필요

### API 응답 처리

```java
// public 버킷: 직접 URL 반환
"https://storage.googleapis.com/finders-public/profiles/123/abc.jpg"

// private 버킷: Signed URL 반환 (1시간 유효)
        "https://storage.googleapis.com/finders-private/temp/orders/456/scans/def.jpg?X-Goog-Signature=..."
```

---

## MySQL DDL

```sql
-- ============================================
-- Finders Database Schema (MySQL 8.0+)
-- ============================================

CREATE
DATABASE IF NOT EXISTS finders
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE
finders;

-- ============================================
-- 1. MEMBER
-- ============================================

CREATE TABLE member
(                                            -- Base 테이블 (공통 필드만)
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    role               VARCHAR(20) NOT NULL, -- Discriminator: USER, OWNER, ADMIN (JPA가 자동 관리)
    name               VARCHAR(20) NOT NULL, -- 실명
    email              VARCHAR(100) NULL,    -- 카카오 비즈앱 통해서 가져옴
    phone              VARCHAR(20) NULL,     -- 카카오 비즈앱 통해서 가져옴
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    refresh_token_hash VARCHAR(500) NULL,    -- JWT Refresh Token Hash (모든 역할 공통)
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         DATETIME NULL,
    PRIMARY KEY (id),
    INDEX              idx_member_role (role),
    CONSTRAINT chk_member_role CHECK (role IN ('USER', 'OWNER', 'ADMIN')),
    CONSTRAINT chk_member_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'WITHDRAWN'))
) ENGINE=InnoDB COMMENT='회원 Base (Joined Table 상속)';

CREATE TABLE member_user
(                                                             -- User 전용 테이블 (소셜 로그인 사용자)
    member_id             BIGINT      NOT NULL,               -- PK & FK → member.id
    nickname              VARCHAR(20) NOT NULL,               -- 카카오 비즈앱 통해서 가져오거나 입력 (닉네임)
    profile_image         VARCHAR(500) NULL,                  -- 카카오 비즈앱 통해서 가져옴
    token_balance         INT UNSIGNED    NOT NULL DEFAULT 3, -- 보유 토큰 (회원가입 시 3개 지급)
    last_token_refresh_at DATETIME NULL,                      -- 마지막 무료 토큰 리프레시 일시
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_user_nickname (nickname),
    CONSTRAINT fk_member_user FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='User 전용 (소셜 로그인, 토큰)';

CREATE TABLE member_owner
(                                              -- Owner 전용 테이블 (현상소 사장님)
    member_id           BIGINT       NOT NULL, -- PK & FK → member.id
    password_hash       VARCHAR(255) NOT NULL, -- BCrypt 해시 (이메일/비밀번호 로그인)
    business_number     VARCHAR(20) NULL,      -- 사업자 번호
    bank_name           VARCHAR(50) NULL,      -- 은행명
    bank_account_number VARCHAR(50) NULL,      -- 계좌번호
    bank_account_holder VARCHAR(50) NULL,      -- 예금주
    PRIMARY KEY (member_id),
    CONSTRAINT fk_member_owner FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Owner 전용 (사업자 정보, 정산 계좌)';

CREATE TABLE member_admin
(                                        -- Admin 전용 테이블 (관리자)
    member_id     BIGINT       NOT NULL, -- PK & FK → member.id
    password_hash VARCHAR(255) NOT NULL, -- BCrypt 해시 (이메일/비밀번호 로그인)
    PRIMARY KEY (member_id),
    CONSTRAINT fk_member_admin FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Admin 전용';

CREATE TABLE social_account
(                                       -- User(role='USER') 전용
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    member_id    BIGINT       NOT NULL, -- FK (User만 연결, Owner/Admin은 소셜 로그인 미사용)
    provider     VARCHAR(20)  NOT NULL, -- KAKAO, APPLE
    provider_id  VARCHAR(100) NOT NULL,
    social_email VARCHAR(100) NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_social_provider (provider, provider_id),
    INDEX        idx_social_member (member_id),
    CONSTRAINT fk_social_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_social_provider CHECK (provider IN ('KAKAO', 'APPLE'))
) ENGINE=InnoDB COMMENT='소셜 계정 (User 전용)';

CREATE TABLE member_address
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    member_id      BIGINT       NOT NULL,               -- FK
    address_name   VARCHAR(50)  NOT NULL,               -- 배송지 이름
    recipient_name VARCHAR(50) NULL,                    -- 배송자 이름
    phone          VARCHAR(20) NULL,
    zipcode        VARCHAR(10)  NOT NULL,               -- 우편번호
    address        VARCHAR(200) NOT NULL,               -- 'ex) 서울 서초구 고무래로 89
    address_detail VARCHAR(100) NULL,                   -- 'ex) 3003동 303호
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE, -- 기본 배송지 여부
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     DATETIME NULL,
    PRIMARY KEY (id),
    INDEX          idx_address_member (member_id, is_default),
    CONSTRAINT fk_address_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB COMMENT='배송지';

CREATE TABLE terms
(                                                      -- 약관 버전 관리
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    type           VARCHAR(20)  NOT NULL,              -- TERMS, PRIVACY, MARKETING, LOCATION
    version        VARCHAR(20)  NOT NULL,              -- 버전 (예: 1.0, 1.1, 2.0)
    title          VARCHAR(200) NOT NULL,              -- 약관 제목
    content        TEXT         NOT NULL,              -- 약관 내용
    is_required    BOOLEAN      NOT NULL DEFAULT TRUE, -- 필수 동의 여부
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE, -- 현재 활성 버전 여부
    effective_date DATE         NOT NULL,              -- 시행일
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_terms_version (type, version),
    INDEX          idx_terms_active (type, is_active),
    CONSTRAINT chk_terms_type CHECK (type IN ('SERVICE', 'PRIVACY', 'LOCATION', 'NOTIFICATION'))
) ENGINE=InnoDB COMMENT='약관 버전';

CREATE TABLE member_agreement
(                                 -- 회원별 약관 동의 이력
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    member_id  BIGINT   NOT NULL, -- FK
    terms_id   BIGINT   NOT NULL, -- FK (약관 버전 참조)
    is_agreed  BOOLEAN  NOT NULL,
    agreed_at  DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX      idx_agreement_member (member_id),
    INDEX      idx_agreement_terms (terms_id),
    CONSTRAINT fk_agreement_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_agreement_terms FOREIGN KEY (terms_id) REFERENCES terms (id)
) ENGINE=InnoDB COMMENT='약관 동의 이력';

CREATE TABLE member_device
(                                       -- FCM(Firebase) 디바이스 토큰 -- 데모데이 전까지 절대 개발 금지
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    member_id    BIGINT       NOT NULL, -- FK
    device_token VARCHAR(500) NOT NULL, -- FCM 토큰
    device_type  VARCHAR(20)  NOT NULL, -- IOS, ANDROID, WEB
    device_name  VARCHAR(100) NULL,     -- 디바이스 이름 (예: iPhone 15, Galaxy S24)
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    last_used_at DATETIME NULL,         -- 마지막 사용 시각
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_token (device_token),
    INDEX        idx_device_member (member_id, is_active),
    CONSTRAINT fk_device_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_device_type CHECK (device_type IN ('IOS', 'ANDROID', 'WEB'))
) ENGINE=InnoDB COMMENT='FCM 디바이스 토큰';

CREATE TABLE token_history
(                                           -- AI 토큰 충전/사용 내역
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    member_id     BIGINT      NOT NULL,
    type          VARCHAR(20) NOT NULL,     -- SIGNUP_BONUS, REFRESH, PURCHASE, USE, REFUND
    amount        INT         NOT NULL,     -- 변동량 (+3, -1 등)
    balance_after INT UNSIGNED    NOT NULL, -- 변동 후 잔액
    related_type  VARCHAR(50) NULL,         -- PHOTO_RESTORATION, PAYMENT
    related_id    BIGINT NULL,              -- 관련 엔티티 ID
    description   VARCHAR(200) NULL,        -- 설명 (예: "회원가입 보너스", "AI 복원 사용")
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX         idx_token_history_member (member_id, created_at DESC),
    INDEX         idx_token_history_type (type),
    CONSTRAINT fk_token_history_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_token_type CHECK (type IN ('SIGNUP_BONUS', 'REFRESH', 'PURCHASE', 'USE', 'REFUND'))
) ENGINE=InnoDB COMMENT='토큰 내역';

CREATE TABLE favorite_photo_lab
( -- 별 표시
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    member_id    BIGINT   NOT NULL,
    photo_lab_id BIGINT   NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite_lab (member_id, photo_lab_id),
    CONSTRAINT fk_fav_lab_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_fav_lab_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id)
) ENGINE=InnoDB COMMENT='관심 현상소';

-- ============================================
-- 2. STORE (현상소)
-- ============================================

CREATE TABLE photo_lab
(
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    owner_id                  BIGINT       NOT NULL,               -- FK → member_owner.member_id (Owner 전용)
    region_id                 BIGINT       NOT NULL,               -- FK region.id (시/군/구)
    name                      VARCHAR(100) NOT NULL,
    description               TEXT NULL,
    phone                     VARCHAR(20) NULL,
    zipcode                   VARCHAR(10) NULL,                    -- 우편번호
    address                   VARCHAR(200) NOT NULL,               -- 'ex) 서울 서초구 고무래로 89
    address_detail            VARCHAR(100) NULL,                   -- 'ex) 3003동 303호
    latitude                  DECIMAL(10, 8) NULL,
    longitude                 DECIMAL(11, 8) NULL,
    work_count                INT UNSIGNED    NOT NULL DEFAULT 0,  -- 작업 총 횟수
    post_count                INT UNSIGNED    NOT NULL DEFAULT 0,  -- 작업 결과물에 필요함
    reservation_count         INT UNSIGNED  NOT NULL DEFAULT 0,    -- 예약 완료 시점에 업데이트
    avg_work_time             INT UNSIGNED    NULL,                -- 주문 완료 시점에(development_order.status = 'COMPLETED') 백엔드에서 직접 계산해서 저장
    status                    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    is_delivery_available     BOOLEAN      NOT NULL DEFAULT FALSE, -- TRUE: 배송가능, FALSE: 배송 불가능
    max_reservations_per_hour INT UNSIGNED NOT NULL DEFAULT 3,     -- 시간당 최대 예약 가능 수 (Owner 조정 가능)
    qr_code_url               VARCHAR(500) NULL,                   -- QR 코드 이미지 URL (현장 주문용)
    created_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at                DATETIME NULL,
    PRIMARY KEY (id),
    INDEX                     idx_lab_status (status),
    INDEX                     idx_lab_location (latitude, longitude),
    FULLTEXT                  INDEX ft_lab_name (name),
    CONSTRAINT fk_lab_owner FOREIGN KEY (owner_id) REFERENCES member (id),
    CONSTRAINT fk_lab_region FOREIGN KEY (region_id) REFERENCES region (id),
    CONSTRAINT chk_lab_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'CLOSED'))
) ENGINE=InnoDB COMMENT='현상소';

CREATE TABLE photo_lab_image
(                                                      -- 현상소마다 이미지 개수가 여러 개이므로 따로 분리
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    photo_lab_id  BIGINT       NOT NULL,               -- FK
    object_path   VARCHAR(500) NOT NULL,               -- GCS object path (예: photo-labs/123/images/abc.jpg)
    display_order INT          NOT NULL DEFAULT 0,
    is_main       BOOLEAN      NOT NULL DEFAULT FALSE, -- 대표 이미지 여부
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX         idx_lab_image (photo_lab_id, is_main),
    CONSTRAINT fk_lab_image FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='현상소 이미지';

CREATE TABLE photo_lab_tag
(                                   -- join table
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    photo_lab_id BIGINT   NOT NULL, -- FK
    tag_id       BIGINT   NOT NULL, -- FK
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lab_tag (photo_lab_id, tag_id),
    INDEX        idx_photo_lab_tag_photo_lab_id (photo_lab_id),
    INDEX        idx_photo_lab_tag_tag_id (tag_id),
    CONSTRAINT fk_photo_lab_tag_photo_lab_id
        FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id) ON DELETE CASCADE,
    CONSTRAINT fk_photo_lab_tag_tag_id
        FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='현상소-태그 조인 테이블';

CREATE TABLE tag
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50) NOT NULL, -- 태그 이름
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB COMMENT='현상소 태그';

CREATE TABLE photo_lab_notice
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    photo_lab_id BIGINT       NOT NULL,                   -- FK
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    notice_type  VARCHAR(20)  NOT NULL DEFAULT 'GENERAL', -- ENUM 처리 GENERAL: 일반공지, EVENT: 이벤트행사공지
    start_date   DATE NULL,                               -- yyyy-MM-dd, 2025-12-23
    end_date     DATE NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX        idx_lab_notice (photo_lab_id),
    CONSTRAINT fk_lab_notice FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id) ON DELETE CASCADE,
    CONSTRAINT chk_lab_notice_type CHECK (notice_type IN ('GENERAL', 'EVENT', 'POLICY'))
) ENGINE=InnoDB COMMENT='현상소 공지';

CREATE TABLE photo_lab_business_hour
(                                                    -- 현상소의 각 요일마다의 일정 등록 1:N 관계
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    photo_lab_id BIGINT      NOT NULL,
    day_of_week  VARCHAR(10) NOT NULL,               -- MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY (java.time.DayOfWeek) 
    open_time    TIME NULL,                          -- HH:mm:ss (예: 09:00:00) 
    close_time   TIME NULL,
    is_closed    BOOLEAN     NOT NULL DEFAULT FALSE, -- FALSE: 영업일, TRUE: 휴무일
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lab_hour (photo_lab_id, day_of_week),
    CONSTRAINT fk_lab_hour FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id) ON DELETE CASCADE,
    CONSTRAINT chk_day_of_week CHECK (day_of_week IN
                                      ('SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'))
) ENGINE=InnoDB COMMENT='영업시간';

CREATE TABLE photo_lab_document
(                                        -- GCP Cloud Storage에 bucket 만들어서 저장해야 할 듯
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    photo_lab_id  BIGINT       NOT NULL,
    document_type VARCHAR(30)  NOT NULL, -- BUSINESS_LICENSE, BUSINESS_PERMIT
    object_path   VARCHAR(500) NOT NULL, -- GCS object path (예: photo-labs/123/documents/BUSINESS_LICENSE/abc.pdf)
    -- 파일의 버전관리가 필요하다. 관리자 입장에서 생각해보면, approved, rejeceted, pending
    file_name     VARCHAR(200) NULL,
    verified_at   DATETIME NULL,         -- 검증 완료 일시
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX         idx_lab_doc (photo_lab_id, document_type),
    CONSTRAINT fk_lab_doc FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id) ON DELETE CASCADE,
    CONSTRAINT chk_doc_type CHECK (document_type IN ('BUSINESS_LICENSE', 'BUSINESS_PERMIT'))
) ENGINE=InnoDB COMMENT='사업자 증빙 서류';

CREATE TABLE region
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    sigungu    VARCHAR(50) NOT NULL, -- 시/군/구
    sido       BIGINT NULL,          -- 상위 시/도 (region.id)
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    INDEX      idx_region_sido (sido),
    UNIQUE KEY uk_region_sigungu_sido (sigungu, sido),
    CONSTRAINT fk_region_sido FOREIGN KEY (sido) REFERENCES region (id)
) ENGINE=InnoDB COMMENT='지역 (시/도, 시/군/구)';

-- ============================================
-- 3. RESERVATION
-- ============================================

CREATE TABLE reservation
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    member_id        BIGINT      NOT NULL,               -- FK
    photo_lab_id     BIGINT      NOT NULL,               -- FK
    reservation_date DATE        NOT NULL,               -- 사용자가 선택한 날짜
    reservation_time TIME        NOT NULL,               -- 사용자가 선택한 시간
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- 작업 내용 선택 (다중 선택 가능)
    is_develop       BOOLEAN     NOT NULL DEFAULT FALSE, -- 현상
    is_scan          BOOLEAN     NOT NULL DEFAULT FALSE, -- 스캔
    is_print         BOOLEAN     NOT NULL DEFAULT FALSE, -- 인화
    roll_count       INT UNSIGNED    NOT NULL DEFAULT 1,
    request_message  VARCHAR(500) NULL,
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME NULL,
    PRIMARY KEY (id),
    INDEX            idx_reservation_member (member_id, status),
    INDEX            idx_reservation_lab (photo_lab_id, reservation_date),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_reservation_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB COMMENT='예약';

CREATE TABLE reservation_slot
(
    id               BIGINT   NOT NULL AUTO_INCREMENT,
    photo_lab_id     BIGINT   NOT NULL,
    reservation_date DATE     NOT NULL,
    reservation_time TIME     NOT NULL,
    max_capacity     INT      NOT NULL,
    reserved_count   INT      NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME NULL,
    PRIMARY KEY (id),

    UNIQUE KEY uk_slot_lab_date_time (photo_lab_id, reservation_date, reservation_time),
    INDEX            idx_slot_lab_date (photo_lab_id, reservation_date),

    CONSTRAINT fk_slot_lab
        FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='예약 슬롯(정원 관리)';



-- ============================================
-- 4. PHOTO (현상/스캔/인화)
-- ============================================

CREATE TABLE development_order
(                                                      -- 현상/스캔 세트로
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NULL,                        -- 예약 없이 현장 주문 가능 (QR 스캔)
    photo_lab_id   BIGINT      NOT NULL,               -- 현상소 (예약 없을 때 필수)
    member_id      BIGINT      NOT NULL,
    order_code     VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    total_photos   INT UNSIGNED    NOT NULL DEFAULT 0, -- 스캔 진행률은 scanned_photo COUNT로 계산
    total_price    INT UNSIGNED    NOT NULL DEFAULT 0, -- 주문 시점 가격 스냅샷 (현상+스캔 패키지)
    completed_at   DATETIME NULL,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dev_order_code (order_code),
    INDEX          idx_dev_order_member (member_id, status),
    INDEX          idx_dev_order_lab (photo_lab_id, status),
    CONSTRAINT fk_dev_order_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT fk_dev_order_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id),
    CONSTRAINT fk_dev_order_member FOREIGN KEY (member_id) REFERENCES member (id),
    is_develop     BOOLEAN     NOT NULL DEFAULT FALSE,
    is_scan        BOOLEAN     NOT NULL DEFAULT FALSE,
    is_print       BOOLEAN     NOT NULL DEFAULT FALSE,
    roll_count     INT UNSIGNED NOT NULL DEFAULT 1,

    CONSTRAINT chk_dev_status CHECK (status IN ('RECEIVED', 'DEVELOPING', 'SCANNING', 'COMPLETED'))
) ENGINE=InnoDB COMMENT='현상 주문';

CREATE TABLE scanned_photo
(                                        -- 1:N 관계
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    order_id      BIGINT       NOT NULL, -- FK development_order
    object_path   VARCHAR(500) NOT NULL, -- GCS object path (예: temp/orders/123/scans/abc.jpg)
    file_name     VARCHAR(200) NULL,
    display_order INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX         idx_scanned_order (order_id),
    CONSTRAINT fk_scanned_order FOREIGN KEY (order_id) REFERENCES development_order (id)
) ENGINE=InnoDB COMMENT='스캔 사진';

CREATE TABLE print_order
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    dev_order_id   BIGINT NULL,          -- 현상 없이 인화만 요청 가능
    photo_lab_id   BIGINT      NOT NULL, -- 현상소
    member_id      BIGINT      NOT NULL,
    order_code     VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price    INT UNSIGNED    NOT NULL DEFAULT 0,
    receipt_method VARCHAR(20) NOT NULL DEFAULT 'PICKUP',
    estimated_at   DATETIME NULL,
    completed_at   DATETIME NULL,
    deposit_receipt_object_path VARCHAR(500) NULL,
    depositor_name              VARCHAR(50)  NULL,
    deposit_bank_name           VARCHAR(50)  NULL,
    payment_submitted_at        DATETIME     NULL,

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_print_order_code (order_code),
    INDEX          idx_print_order_member (member_id, status),
    INDEX          idx_print_order_lab (photo_lab_id, status),
    CONSTRAINT fk_print_order_dev FOREIGN KEY (dev_order_id) REFERENCES development_order (id),
    CONSTRAINT fk_print_order_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id),
    CONSTRAINT fk_print_order_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_print_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PRINTING', 'READY', 'SHIPPED', 'COMPLETED')),
    CONSTRAINT chk_receipt_method CHECK (receipt_method IN ('PICKUP', 'DELIVERY'))
) ENGINE=InnoDB COMMENT='인화 주문';

CREATE TABLE print_order_item
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    print_order_id BIGINT      NOT NULL, -- FK

    film_type      VARCHAR(20) NOT NULL,
    paper_type     VARCHAR(30) NOT NULL,
    print_method   VARCHAR(20) NOT NULL,
    size           VARCHAR(20) NOT NULL,
    frame_type     VARCHAR(20) NOT NULL,

    unit_price     INT UNSIGNED NOT NULL,
    total_price    INT UNSIGNED NOT NULL,

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX          idx_print_item_order (print_order_id),
    CONSTRAINT fk_print_item_order FOREIGN KEY (print_order_id) REFERENCES print_order (id)
) ENGINE=InnoDB COMMENT='인화 옵션/가격 상세';

CREATE TABLE print_order_photo
(
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    print_order_id  BIGINT   NOT NULL,
    scanned_photo_id BIGINT  NOT NULL,
    quantity        INT      NOT NULL,

    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_pop_order_photo (print_order_id, scanned_photo_id),

    CONSTRAINT fk_pop_print_order FOREIGN KEY (print_order_id) REFERENCES print_order (id),
    CONSTRAINT fk_pop_scanned_photo FOREIGN KEY (scanned_photo_id) REFERENCES scanned_photo (id)
) ENGINE=InnoDB COMMENT='인화 주문-스캔사진 매핑(수량 포함)';


CREATE TABLE delivery
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    print_order_id  BIGINT       NOT NULL,
    recipient_name  VARCHAR(50)  NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    zipcode         VARCHAR(10)  NOT NULL,
    address         VARCHAR(200) NOT NULL,
    address_detail  VARCHAR(100) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    tracking_number VARCHAR(50) NULL,
    carrier         VARCHAR(50) NULL,
    delivery_fee    INT UNSIGNED    NOT NULL DEFAULT 0,
    shipped_at      DATETIME NULL,
    delivered_at    DATETIME NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_delivery_order (print_order_id),
    INDEX           idx_delivery_status (status),
    CONSTRAINT fk_delivery_order FOREIGN KEY (print_order_id) REFERENCES print_order (id),
    CONSTRAINT chk_delivery_status CHECK (status IN ('PENDING', 'PREPARING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED'))
) ENGINE=InnoDB COMMENT='배송';

CREATE TABLE photo_restoration
(                                                                    -- Replicate AI 사용
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    member_id               BIGINT       NOT NULL,
    original_path           VARCHAR(500) NOT NULL,                   -- 원본 이미지 GCS 경로
    mask_path               VARCHAR(500) NOT NULL,                   -- 마스크 이미지 GCS 경로 (프론트에서 전송)
    restored_path           VARCHAR(500) NULL,                       -- 복원된 이미지 GCS 경로
    restored_width          INT NULL,                                -- 복원된 이미지 너비 (픽셀)
    restored_height         INT NULL,                                -- 복원된 이미지 높이 (픽셀)
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    replicate_prediction_id VARCHAR(100) NULL,                       -- Replicate API prediction ID (webhook 매칭용)
    -- 토큰 관련
    token_used              INT UNSIGNED    NOT NULL DEFAULT 1,      -- 사용된 토큰 수 (복원 완료 시 차감)
    -- 에러 정보
    error_message           VARCHAR(500) NULL,                       -- 실패 시 에러 메시지
    -- 피드백 (AI 품질 개선용)
    feedback_rating         VARCHAR(10) NULL,                        -- GOOD, BAD
    feedback_comment        VARCHAR(500) NULL,                       -- 피드백 코멘트 (선택)
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX                   idx_restoration_member (member_id, status),
    INDEX                   idx_restoration_prediction (replicate_prediction_id),
    CONSTRAINT fk_restoration_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_restoration_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_feedback_rating CHECK (feedback_rating IS NULL OR feedback_rating IN ('GOOD', 'BAD'))
) ENGINE=InnoDB COMMENT='AI 사진 복원';

-- ============================================
-- 5. COMMUNITY
-- ============================================

CREATE TABLE post
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    member_id         BIGINT      NOT NULL,               -- FK
    photo_lab_id      BIGINT NULL,                        -- FK (현상소 이용 시)
    is_self_developed BOOLEAN     NOT NULL DEFAULT FALSE, -- TRUE: 자가현상, FALSE: 현상소 이용
    title             VARCHAR(200) NULL,
    content           TEXT        NOT NULL,
    lab_review        VARCHAR(500) NULL,                  -- 현상소 리뷰 (현상소 이용 시)
    like_count        INT UNSIGNED    NOT NULL DEFAULT 0,
    comment_count     INT UNSIGNED    NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        DATETIME NULL,
    PRIMARY KEY (id),
    INDEX             idx_post_member (member_id, status),
    INDEX             idx_post_lab (photo_lab_id),
    INDEX             idx_post_created (created_at DESC),
    FULLTEXT          INDEX ft_post_content (title, content),
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_post_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id),
    CONSTRAINT chk_post_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),
    CONSTRAINT chk_post_lab_required CHECK (
        (is_self_developed = TRUE AND photo_lab_id IS NULL) OR
        (is_self_developed = FALSE AND photo_lab_id IS NOT NULL)
        )
) ENGINE=InnoDB COMMENT='게시글/리뷰';

CREATE TABLE post_image
(                                        -- 1:N
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    post_id       BIGINT       NOT NULL, -- FK
    object_path   VARCHAR(500) NOT NULL, -- GCS object path (예: posts/123/abc.jpg)
    display_order INT          NOT NULL DEFAULT 0,
    width         INT          NOT NULL, -- 이미지 너비 (픽셀)
    height        INT          NOT NULL, -- 이미지 높이 (픽셀)
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX         idx_post_image (post_id),
    CONSTRAINT fk_post_image FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='게시글 이미지';

CREATE TABLE comments
( -- 댓글
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    post_id    BIGINT        NOT NULL,
    member_id  BIGINT        NOT NULL,
    content    VARCHAR(1000) NOT NULL,
    status     VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    INDEX      idx_comments_post (post_id, created_at),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_comments_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_comments_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED'))
) ENGINE=InnoDB COMMENT='댓글';

CREATE TABLE post_like
( -- 하트 표시
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    post_id    BIGINT   NOT NULL,
    member_id  BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_like (post_id, member_id),
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    CONSTRAINT fk_like_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB COMMENT='좋아요';

-- ============================================
-- 6. INQUIRY
-- ============================================

CREATE TABLE inquiry
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    member_id    BIGINT       NOT NULL,
    photo_lab_id BIGINT NULL,
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX        idx_inquiry_member (member_id, status),
    INDEX        idx_inquiry_lab (photo_lab_id),
    CONSTRAINT fk_inquiry_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_inquiry_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id),
    CONSTRAINT chk_inquiry_status CHECK (status IN ('PENDING', 'ANSWERED', 'CLOSED'))
) ENGINE=InnoDB COMMENT='1:1 문의';

CREATE TABLE inquiry_image
(                                        -- 문의 첨부 이미지 (최대 5개)
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    inquiry_id    BIGINT       NOT NULL, -- FK
    object_path   VARCHAR(500) NOT NULL, -- GCS object path (예: inquiries/123/abc.jpg)
    display_order INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX         idx_inquiry_image (inquiry_id),
    CONSTRAINT fk_inquiry_image FOREIGN KEY (inquiry_id) REFERENCES inquiry (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='문의 이미지';

CREATE TABLE inquiry_reply
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    inquiry_id BIGINT   NOT NULL,
    replier_id BIGINT   NOT NULL, -- 답변자 (ADMIN: 서비스 문의, OWNER: 현상소 문의)
    content    TEXT     NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX      idx_reply_inquiry (inquiry_id),
    CONSTRAINT fk_reply_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry (id),
    CONSTRAINT fk_reply_replier FOREIGN KEY (replier_id) REFERENCES member (id)
) ENGINE=InnoDB COMMENT='문의 답변';

-- ============================================
-- 7. COMMON
-- ============================================

CREATE TABLE notice
(                                                    -- 전체 회원 공지사항 게시판 
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    notice_type VARCHAR(20)  NOT NULL DEFAULT 'GENERAL',
    is_pinned   BOOLEAN      NOT NULL DEFAULT FALSE, -- 고정되었는지
    view_count  INT UNSIGNED    NOT NULL DEFAULT 0,  -- 공지에 view count가 필요할까..?
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME NULL,
    PRIMARY KEY (id),
    INDEX       idx_notice_pinned (is_pinned DESC, created_at DESC),
    CONSTRAINT chk_notice_type CHECK (notice_type IN ('GENERAL', 'EVENT', 'POLICY'))
) ENGINE=InnoDB COMMENT='공지사항';

CREATE TABLE promotion
(                                         -- 메인페이지 프로모션 배너 부분 -- 데모데이 전까지 절대 개발 금지
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    photo_lab_id   BIGINT       NOT NULL,
    title          VARCHAR(200) NOT NULL,
    description    VARCHAR(500) NULL,
    object_path    VARCHAR(500) NOT NULL, -- GCS object path (예: promotions/123/abc.jpg)
    promotion_type VARCHAR(20)  NOT NULL DEFAULT 'BANNER',
    display_order  INT          NOT NULL DEFAULT 0,
    start_date     DATETIME     NOT NULL,
    end_date       DATETIME     NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX          idx_promotion_active (is_active, start_date, end_date),
    CONSTRAINT fk_promotion_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab (id),
    CONSTRAINT chk_promotion_type CHECK (promotion_type IN ('BANNER', 'POPUP', 'EVENT'))
) ENGINE=InnoDB COMMENT='프로모션';

CREATE TABLE notification
( -- 회원별 개인 알림(앱 푸시/ 알림센터) -- 데모데이 전까지 절대 개발 금지
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    member_id         BIGINT       NOT NULL,
    title             VARCHAR(100) NOT NULL,
    content           VARCHAR(500) NOT NULL,
    notification_type VARCHAR(20)  NOT NULL,
    related_id        BIGINT NULL COMMENT '관련 엔티티 ID',
    related_type      VARCHAR(50) NULL COMMENT '관련 엔티티 타입',
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX             idx_notification_member (member_id, is_read, created_at DESC),
    CONSTRAINT fk_notification_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_notification_type CHECK (notification_type IN
                                            ('ORDER', 'RESERVATION', 'COMMUNITY', 'MARKETING', 'NOTICE'))
) ENGINE=InnoDB COMMENT='알림';

CREATE TABLE payment
(                                                -- 포트원 V2 결제 연동
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    member_id          BIGINT       NOT NULL,
    -- 주문 정보
    order_type         VARCHAR(20)  NOT NULL,    -- TOKEN_PURCHASE, DEVELOPMENT_ORDER, PRINT_ORDER
    related_order_id   BIGINT NULL,              -- development_order.id 또는 print_order.id (토큰 구매 시 NULL)
    payment_id         VARCHAR(64)  NOT NULL,    -- 결제 건 ID (우리가 생성, 포트원에 전달)
    order_name         VARCHAR(100) NOT NULL,    -- 주문명 (예: "AI 복원 토큰 10개")
    -- 금액 정보
    amount             INT UNSIGNED    NOT NULL, -- 결제 요청 금액
    token_amount       INT UNSIGNED    NULL,     -- 구매한 토큰 수 (TOKEN_PURCHASE 시)
    -- 포트원 V2 (승인 후 저장)
    transaction_id     VARCHAR(100) NULL,        -- 포트원 채번 ID (V1의 imp_uid에 해당)
    pg_tx_id           VARCHAR(100) NULL,        -- PG사 거래 ID
    pg_provider        VARCHAR(30) NULL,         -- PG사 (TOSSPAYMENTS, KCP, KAKAOPAY 등)
    -- 결제 수단
    method             VARCHAR(30) NULL,         -- CARD, TRANSFER, VIRTUAL_ACCOUNT, MOBILE, EASY_PAY
    status             VARCHAR(30)  NOT NULL DEFAULT 'READY',
    -- 카드 정보 (CARD, EASY_PAY 결제 시)
    card_company       VARCHAR(20) NULL,         -- 카드사 (삼성, 현대 등)
    card_number        VARCHAR(20) NULL,         -- 마스킹된 카드번호 (1234****5678)
    approve_no         VARCHAR(20) NULL,         -- 승인번호 (환불/분쟁 시 필수)
    installment_months INT NULL,                 -- 할부 개월수 (0=일시불)
    receipt_url        VARCHAR(500) NULL,        -- 영수증 URL
    -- 시간 정보
    requested_at       DATETIME NULL,            -- 결제 요청 시각
    paid_at            DATETIME NULL,            -- 결제 완료 시각
    -- 실패/취소 정보
    fail_code          VARCHAR(50) NULL,
    fail_message       VARCHAR(200) NULL,
    cancelled_at       DATETIME NULL,
    cancel_reason      VARCHAR(200) NULL,
    cancel_amount      INT UNSIGNED    NULL,     -- 취소 금액 (부분취소 대응)
    -- 공통
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_id (payment_id),
    UNIQUE KEY uk_transaction_id (transaction_id),
    INDEX              idx_payment_member (member_id, status),
    INDEX              idx_payment_order_type (order_type, related_order_id),
    INDEX              idx_payment_status (status),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT chk_payment_status CHECK (status IN (
                                                    'READY', 'PENDING', 'VIRTUAL_ACCOUNT_ISSUED', 'PAID',
                                                    'FAILED', 'PARTIAL_CANCELLED', 'CANCELLED'
        )),
    CONSTRAINT chk_order_type CHECK (order_type IN ('TOKEN_PURCHASE', 'DEVELOPMENT_ORDER', 'PRINT_ORDER')),
    CONSTRAINT chk_payment_method CHECK (method IS NULL OR method IN (
        'CARD', 'TRANSFER', 'VIRTUAL_ACCOUNT', 'EASY_PAY'
    )),
    CONSTRAINT chk_payment_data CHECK (
        (order_type = 'TOKEN_PURCHASE' AND token_amount IS NOT NULL AND related_order_id IS NULL) OR
        (order_type IN ('DEVELOPMENT_ORDER', 'PRINT_ORDER') AND related_order_id IS NOT NULL AND token_amount IS NULL)
        )
) ENGINE=InnoDB COMMENT='결제 (포트원 V2)';
```

---

## 화면-테이블 매핑

| 화면                   | 테이블                                                                                |
|----------------------|------------------------------------------------------------------------------------|
| CM-020~022 로그인/가입    | member, member_user, social_account, member_agreement                              |
| HM-010 메인 홈          | photo_lab, promotion                                                               |
| HM-021~025 사진 복원     | photo_restoration, token_history, member_user (token_balance)                      |
| PL-010~011 현상소 탐색    | photo_lab, photo_lab_tag, tag, region                                              |
| PL-020~021 현상소 상세/예약 | photo_lab_*, reservation, payment                                                  |
| CO-020~030 사진수다      | post (자가현상 여부), post_image, comment, post_like                                     |
| PM-000~018 현상관리      | development_order, scanned_photo, print_order, print_order_item, delivery, payment |
| UR-010~025 마이페이지     | member, member_user, social_account                                                |
| UR-030~040 관심목록      | favorite_photo_lab, post_like                                                      |
| UR-060~062 배송지       | member_address                                                                     |
| UR-070~081 공지/문의     | notice, inquiry, inquiry_reply                                                     |
| 알림 센터                | notification                                                                       |
| 사업자 등록               | member_owner, photo_lab, photo_lab_document                                        |

---

## 변경 이력

| 버전  | 날짜       | 변경 내용 |
|-------|-----------|----------|
| **3.0.0** | **2026-01-18** | 버전 체계 재정비: 이전 변경 이력 아카이빙 (Git 히스토리 참조), 현재 스키마 기준 메이저 버전 설정 (35개 테이블) |
| **3.0.1** | **2026-01-19** | 인화 도메인 스키마 반영: `development_order`에 작업 선택 컬럼(`is_develop/is_scan/is_print/roll_count`) 추가, `print_order`에 입금 캡처/입금자/은행/제출시각 컬럼 추가, `print_order_item` 구조를 옵션 단위로 변경(`film_type/paper_type/print_method/size/frame_type`), 인화 대상 사진 매핑 테이블 `print_order_photo` 추가 |
---

**참고**: v2.5.1 이전 변경 이력은 Git 커밋 히스토리에서 확인 가능합니다.
