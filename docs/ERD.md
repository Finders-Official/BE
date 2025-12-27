# Finders ERD

> 필름 현상소 예약 서비스 데이터베이스 설계서
> v1.9.2 | 2025-12-28

---

## 테이블 목록 (29개)

| 도메인 | 테이블 | 설명 |
|--------|--------|------|
| **member** | `member` | 회원 정보 (토큰 잔액 포함) |
| | `social_account` | 소셜 로그인 연동 (카카오/애플) |
| | `member_address` | 배송지 주소 |
| | `member_agreement` | 약관 동의 이력 |
| | `token_history` | AI 토큰 충전/사용 내역 |
| **store** | `photo_lab` | 현상소 정보 |
| | `photo_lab_image` | 현상소 이미지 |
| | `photo_lab_keyword` | 현상소 키워드/태그 |
| | `photo_lab_notice` | 현상소 공지사항 |
| | `photo_lab_business_hour` | 영업시간 |
| | `photo_lab_document` | 사업자 증빙 서류 |
| **reservation** | `reservation` | 예약 정보 |
| **photo** | `development_order` | 현상 주문 |
| | `scanned_photo` | 스캔된 사진 |
| | `print_order` | 인화 주문 |
| | `print_order_item` | 인화 주문 상세 |
| | `delivery` | 배송 정보 |
| | `photo_restoration` | AI 사진 복원 요청 |
| **community** | `post` | 게시글/리뷰 (자가현상 여부 포함) |
| | `post_image` | 게시글 이미지 |
| | `comment` | 댓글 |
| | `post_like` | 좋아요 |
| | `favorite_photo_lab` | 관심 현상소 |
| **inquiry** | `inquiry` | 1:1 문의 |
| | `inquiry_reply` | 문의 답변 |
| **common** | `notice` | 공지사항 |
| | `promotion` | 프로모션/배너 |
| | `film_content` | 필름 콘텐츠 |
| | `notification` | 알림 |
| | `payment` | 결제 정보 |

---

## ERD 관계도

```
member ─┬─ 1:N ─ social_account
        ├─ 1:N ─ member_address
        ├─ 1:N ─ member_agreement (약관 동의)
        ├─ 1:N ─ token_history (토큰 내역)
        ├─ 1:N ─ reservation ──── 0..1:1 ─ development_order ─┬─ 1:N ─ scanned_photo
        │                                                     └─ 0..1:N ─ print_order ─┬─ 1:N ─ print_order_item
        │                                                                              └─ 0..1:1 ─ delivery
        ├─ 1:N ─ development_order (현장 주문, 예약 없이)
        ├─ 1:N ─ print_order (현장 주문, 현상 없이)
        ├─ 1:N ─ photo_restoration (AI 복원, 토큰 사용)
        ├─ 1:N ─ post ─┬─ 1:N ─ post_image
        │              ├─ 1:N ─ comment
        │              └─ 1:N ─ post_like
        ├─ 1:N ─ favorite_photo_lab
        ├─ 1:N ─ inquiry ─── 1:N ─ inquiry_reply
        ├─ 1:N ─ notification
        └─ 1:N ─ payment (결제, 토큰 구매)

photo_lab ─┬─ 1:N ─ photo_lab_image
           ├─ 1:N ─ photo_lab_keyword
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
// 회원
MemberRole: USER, OWNER, ADMIN
MemberStatus: ACTIVE, SUSPENDED, WITHDRAWN
SocialProvider: KAKAO, APPLE

// 현상소
PhotoLabStatus: PENDING, ACTIVE, SUSPENDED, CLOSED
DayOfWeek: SUN, MON, TUE, WED, THU, FRI, SAT

// 예약/주문
ReservationStatus: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
DevelopmentOrderStatus: RECEIVED, DEVELOPING, SCANNING, COMPLETED
PrintOrderStatus: PENDING, CONFIRMED, PRINTING, READY, SHIPPED, COMPLETED
ReceiptMethod: PICKUP, DELIVERY
DeliveryStatus: PENDING, PREPARING, SHIPPED, IN_TRANSIT, DELIVERED

// 커뮤니티
PostStatus: ACTIVE, HIDDEN, DELETED

// 공지/알림
NoticeType: GENERAL, EVENT, POLICY
NotificationType: ORDER, RESERVATION, COMMUNITY, MARKETING, NOTICE
PromotionType: BANNER, POPUP, EVENT

// 인화 옵션
PaperType: GLOSSY, MATTE, SILK, LUSTER
PrintMethod: INKJET, LASER, SILVER_HALIDE
PrintProcess: NORMAL, BORDER, BORDERLESS

// 약관/결제/복원 (v1.2.0 추가)
AgreementType: TERMS, PRIVACY, MARKETING, LOCATION
DocumentType: BUSINESS_LICENSE, BUSINESS_PERMIT
RestorationStatus: PENDING, PROCESSING, COMPLETED, FAILED
FeedbackRating: GOOD, BAD

// 결제/토큰 (v1.3.0 추가, v1.6.0 포트원 전환)
PaymentMethod: CARD, EASY_PAY, ON_SITE  // 포트원 카드/간편결제 + 현장결제(확장고려)
PaymentStatus: PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED
OrderType: DEVELOPMENT_ORDER, PRINT_ORDER, TOKEN_PURCHASE
TokenHistoryType: SIGNUP_BONUS, REFRESH, PURCHASE, USE, REFUND
```

---

## MySQL DDL

```sql
-- ============================================
-- Finders Database Schema (MySQL 8.0+)
-- ============================================

CREATE DATABASE IF NOT EXISTS finders
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE finders;

-- ============================================
-- 1. MEMBER
-- ============================================

CREATE TABLE member (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nickname        VARCHAR(20)     NOT NULL,   -- social 로그인에서 받아옴
    email           VARCHAR(100)    NULL,       -- social 로그인에서 받아야 하는데, 카카오에 비즈앱 등록 전에는 못 가져옴... PM에게 물어봐야할 듯
    phone           VARCHAR(20)     NULL,       -- 인증 API 구현해야함
    profile_image   VARCHAR(500)    NULL,       -- social 로그인에서 받아옴
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    -- AI 토큰 관련
    token_balance   INT UNSIGNED    NOT NULL DEFAULT 3,     -- 보유 토큰 (회원가입 시 3개 지급)
    last_token_refresh_at DATETIME  NULL,                   -- 마지막 무료 토큰 리프레시 일시
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_nickname (nickname),
    CONSTRAINT chk_member_role CHECK (role IN ('USER', 'OWNER', 'ADMIN')),
    CONSTRAINT chk_member_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'WITHDRAWN'))
) ENGINE=InnoDB COMMENT='회원';

CREATE TABLE social_account (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,   -- FK
    provider        VARCHAR(20)     NOT NULL,   -- KAKAO, APPLE만 사용
    provider_id     VARCHAR(100)    NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_social_provider (provider, provider_id),
    CONSTRAINT fk_social_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_social_provider CHECK (provider IN ('KAKAO', 'APPLE'))
) ENGINE=InnoDB COMMENT='소셜 계정';

CREATE TABLE member_address (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,   -- FK
    address_name    VARCHAR(50)     NOT NULL,   -- 배송지 이름
    recipient_name  VARCHAR(50)     NOT NULL,   -- 배송자 이름
    phone           VARCHAR(20)     NOT NULL, 
    zipcode         VARCHAR(10)     NOT NULL,   -- 우편번호
    address         VARCHAR(200)    NOT NULL,   -- 'ex) 서울 서초구 고무래로 89
    address_detail  VARCHAR(100)    NULL,       -- 'ex) 3003동 303호
    is_default      BOOLEAN         NOT NULL DEFAULT FALSE, -- 기본 배송지 여부
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_address_member (member_id, is_default),
    CONSTRAINT fk_address_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB COMMENT='배송지';

CREATE TABLE member_agreement ( -- 개인정보 이용약관
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,   -- FK
    agreement_type  VARCHAR(20)     NOT NULL,   -- TERMS, PRIVACY, MARKETING, LOCATION
    is_agreed       BOOLEAN         NOT NULL,
    agreed_at       DATETIME        NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_agreement_member (member_id),
    CONSTRAINT fk_agreement_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_agreement_type CHECK (agreement_type IN ('TERMS', 'PRIVACY', 'MARKETING', 'LOCATION'))
) ENGINE=InnoDB COMMENT='약관 동의 이력';

CREATE TABLE token_history (    -- AI 토큰 충전/사용 내역
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    type            VARCHAR(20)     NOT NULL,   -- SIGNUP_BONUS, REFRESH, PURCHASE, USE, REFUND
    amount          INT             NOT NULL,   -- 변동량 (+3, -1 등)
    balance_after   INT UNSIGNED    NOT NULL,   -- 변동 후 잔액
    related_type    VARCHAR(50)     NULL,       -- PHOTO_RESTORATION, PAYMENT
    related_id      BIGINT          NULL,       -- 관련 엔티티 ID
    description     VARCHAR(200)    NULL,       -- 설명 (예: "회원가입 보너스", "AI 복원 사용")
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_token_history_member (member_id, created_at DESC),
    INDEX idx_token_history_type (type),
    CONSTRAINT fk_token_history_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_token_type CHECK (type IN ('SIGNUP_BONUS', 'REFRESH', 'PURCHASE', 'USE', 'REFUND'))
) ENGINE=InnoDB COMMENT='토큰 내역';

-- ============================================
-- 2. STORE (현상소)
-- ============================================

CREATE TABLE photo_lab (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    owner_id        BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT            NULL,
    phone           VARCHAR(20)     NULL,
    zipcode         VARCHAR(10)     NULL,       -- 우편번호
    address         VARCHAR(200)    NOT NULL,   -- 'ex) 서울 서초구 고무래로 89
    address_detail  VARCHAR(100)    NULL,       -- 'ex) 3003동 303호
    latitude        DECIMAL(10, 8)  NULL,
    longitude       DECIMAL(11, 8)  NULL,
    rating          DECIMAL(2, 1)   NOT NULL DEFAULT 0.0,
    post_count    INT UNSIGNED    NOT NULL DEFAULT 0, -- 작업 결과물에 필요함
    reservation_count INT UNSIGNED  NOT NULL DEFAULT 0, -- 예약 완료 시점에 업데이트
    avg_work_time   INT UNSIGNED    NULL,       -- 주문 완료 시점에(development_order.status = 'COMPLETED') 백엔드에서 직접 계산해서 저장
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    is_delivery_available BOOLEAN    NOT NULL DEFAULT FALSE, -- TRUE: 배송가능, FALSE: 배송 불가능
    business_number VARCHAR(20)     NULL,       -- 사업자 번호
    qr_code_url     VARCHAR(500)    NULL,       -- QR 코드 이미지 URL (현장 주문용)
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_lab_status (status),
    INDEX idx_lab_rating (rating DESC),
    INDEX idx_lab_location (latitude, longitude),
    FULLTEXT INDEX ft_lab_name (name),
    CONSTRAINT fk_lab_owner FOREIGN KEY (owner_id) REFERENCES member(id),
    CONSTRAINT chk_lab_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'CLOSED'))
) ENGINE=InnoDB COMMENT='현상소';

CREATE TABLE photo_lab_image (  -- 현상소마다 이미지 개수가 여러 개이므로 따로 분리
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    photo_lab_id    BIGINT          NOT NULL,   -- FK
    image_url       VARCHAR(500)    NOT NULL,   -- GCP Cloud Storage
    display_order   INT             NOT NULL DEFAULT 0,
    is_main         BOOLEAN         NOT NULL DEFAULT FALSE, -- 대표 이미지 여부
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_lab_image (photo_lab_id, is_main),
    CONSTRAINT fk_lab_image FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='현상소 이미지';

CREATE TABLE photo_lab_keyword (    -- 1:N (5개 고정 키워드)
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    photo_lab_id    BIGINT          NOT NULL,   -- FK
    keyword         VARCHAR(50)     NOT NULL,   -- 고정 키워드: 따뜻한 색감, 청량한, 빈티지한, 영화용 필름, 택배 접수
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lab_keyword (photo_lab_id, keyword),
    CONSTRAINT fk_lab_keyword FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='현상소 키워드';

CREATE TABLE photo_lab_notice (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    photo_lab_id    BIGINT          NOT NULL,   -- FK
    title           VARCHAR(200)    NOT NULL,
    content         TEXT            NOT NULL,
    notice_type     VARCHAR(20)     NOT NULL DEFAULT 'GENERAL', -- ENUM 처리 GENERAL: 일반공지, EVENT: 이벤트행사공지
    start_date      DATE            NULL,   -- yyyy-MM-dd, 2025-12-23
    end_date        DATE            NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_lab_notice (photo_lab_id),
    CONSTRAINT fk_lab_notice FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id) ON DELETE CASCADE,
    CONSTRAINT chk_lab_notice_type CHECK (notice_type IN ('GENERAL', 'EVENT', 'POLICY'))
) ENGINE=InnoDB COMMENT='현상소 공지';

CREATE TABLE photo_lab_business_hour (  -- 현상소의 각 요일마다의 일정 등록 1:N 관계
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    photo_lab_id    BIGINT          NOT NULL,
    day_of_week     VARCHAR(3)      NOT NULL,   -- SUN, MON, TUE, WED, THU, FRI, SAT
    open_time       TIME            NULL,       -- HH:mm:ss (예: 09:00:00)
    close_time      TIME            NULL,
    is_closed       BOOLEAN         NOT NULL DEFAULT FALSE, -- FALSE: 영업일, TRUE: 휴무일
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lab_hour (photo_lab_id, day_of_week),
    CONSTRAINT fk_lab_hour FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id) ON DELETE CASCADE,
    CONSTRAINT chk_day_of_week CHECK (day_of_week IN ('SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'))
) ENGINE=InnoDB COMMENT='영업시간';

CREATE TABLE photo_lab_document (   -- GCP Cloud Storage에 bucket 만들어서 저장해야 할 듯
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    photo_lab_id    BIGINT          NOT NULL,
    document_type   VARCHAR(30)     NOT NULL,   -- BUSINESS_LICENSE, BUSINESS_PERMIT
    file_url        VARCHAR(500)    NOT NULL,   -- GCP Cloud Storage에 documents/{photo_lab_id}/business-license/{document_id}_{number}.pdf 의 이름으로 저장해야할 듯
    -- 파일의 버전관리가 필요하다. 관리자 입장에서 생각해보면, approved, rejeceted, pending
    file_name       VARCHAR(200)    NULL,
    verified_at     DATETIME        NULL,       -- 검증 완료 일시
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_lab_doc (photo_lab_id, document_type),
    CONSTRAINT fk_lab_doc FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id) ON DELETE CASCADE,
    CONSTRAINT chk_doc_type CHECK (document_type IN ('BUSINESS_LICENSE', 'BUSINESS_PERMIT'))
) ENGINE=InnoDB COMMENT='사업자 증빙 서류';

-- ============================================
-- 3. RESERVATION
-- ============================================

CREATE TABLE reservation (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,   -- FK
    photo_lab_id    BIGINT          NOT NULL,   -- FK
    reservation_date DATE           NOT NULL,   -- 사용자가 선택한 날짜
    reservation_time TIME           NOT NULL,   -- 사용자가 선택한 시간
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- 작업 내용 선택 (다중 선택 가능)
    is_develop      BOOLEAN         NOT NULL DEFAULT FALSE,  -- 현상
    is_scan         BOOLEAN         NOT NULL DEFAULT FALSE,  -- 스캔
    is_print        BOOLEAN         NOT NULL DEFAULT FALSE,  -- 인화
    roll_count      INT UNSIGNED    NOT NULL DEFAULT 1,
    request_message VARCHAR(500)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_reservation_member (member_id, status),
    INDEX idx_reservation_lab (photo_lab_id, reservation_date),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_reservation_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB COMMENT='예약';

-- ============================================
-- 4. PHOTO (현상/스캔/인화)
-- ============================================

CREATE TABLE development_order (    -- 현상/스캔 세트로
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    reservation_id  BIGINT          NULL,       -- 예약 없이 현장 주문 가능 (QR 스캔)
    photo_lab_id    BIGINT          NOT NULL,   -- 현상소 (예약 없을 때 필수)
    member_id       BIGINT          NOT NULL,
    order_code      VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'RECEIVED',
    total_photos    INT UNSIGNED    NOT NULL DEFAULT 0,  -- 스캔 진행률은 scanned_photo COUNT로 계산
    total_price     INT UNSIGNED    NOT NULL DEFAULT 0,  -- 주문 시점 가격 스냅샷 (현상+스캔 패키지)
    completed_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dev_order_code (order_code),
    INDEX idx_dev_order_member (member_id, status),
    INDEX idx_dev_order_lab (photo_lab_id, status),
    CONSTRAINT fk_dev_order_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    CONSTRAINT fk_dev_order_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id),
    CONSTRAINT fk_dev_order_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_dev_status CHECK (status IN ('RECEIVED', 'DEVELOPING', 'SCANNING', 'COMPLETED'))
) ENGINE=InnoDB COMMENT='현상 주문';

CREATE TABLE scanned_photo (    -- 1:N 관계
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    order_id        BIGINT          NOT NULL,   -- FK development_order
    image_url       VARCHAR(500)    NOT NULL,   -- GCP Cloud Storage
    file_name       VARCHAR(200)    NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_scanned_order (order_id),
    CONSTRAINT fk_scanned_order FOREIGN KEY (order_id) REFERENCES development_order(id)
) ENGINE=InnoDB COMMENT='스캔 사진';

CREATE TABLE print_order (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    dev_order_id    BIGINT          NULL,       -- 현상 없이 인화만 요청 가능
    photo_lab_id    BIGINT          NOT NULL,   -- 현상소
    member_id       BIGINT          NOT NULL,
    order_code      VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    total_price     INT UNSIGNED    NOT NULL DEFAULT 0,
    receipt_method  VARCHAR(20)     NOT NULL DEFAULT 'PICKUP',
    estimated_at    DATETIME        NULL,
    completed_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_print_order_code (order_code),
    INDEX idx_print_order_member (member_id, status),
    INDEX idx_print_order_lab (photo_lab_id, status),
    CONSTRAINT fk_print_order_dev FOREIGN KEY (dev_order_id) REFERENCES development_order(id),
    CONSTRAINT fk_print_order_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id),
    CONSTRAINT fk_print_order_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_print_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PRINTING', 'READY', 'SHIPPED', 'COMPLETED')),
    CONSTRAINT chk_receipt_method CHECK (receipt_method IN ('PICKUP', 'DELIVERY'))
) ENGINE=InnoDB COMMENT='인화 주문';

CREATE TABLE print_order_item (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    print_order_id  BIGINT          NOT NULL,   -- FK
    scanned_photo_id BIGINT         NOT NULL,
    paper_type      VARCHAR(20)     NOT NULL DEFAULT 'GLOSSY',
    print_method    VARCHAR(20)     NOT NULL DEFAULT 'INKJET', -- 인화방식 (INKJET, LASER, etc)
    size            VARCHAR(20)     NOT NULL,
    process         VARCHAR(20)     NOT NULL DEFAULT 'NORMAL',
    quantity        INT UNSIGNED    NOT NULL DEFAULT 1,
    unit_price      INT UNSIGNED    NOT NULL,
    total_price     INT UNSIGNED    NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_print_item_order (print_order_id),
    CONSTRAINT fk_print_item_order FOREIGN KEY (print_order_id) REFERENCES print_order(id),
    CONSTRAINT fk_print_item_photo FOREIGN KEY (scanned_photo_id) REFERENCES scanned_photo(id),
    CONSTRAINT chk_paper_type CHECK (paper_type IN ('GLOSSY', 'MATTE', 'SILK', 'LUSTER')),
    CONSTRAINT chk_print_method CHECK (print_method IN ('INKJET', 'LASER', 'SILVER_HALIDE')),
    CONSTRAINT chk_print_process CHECK (process IN ('NORMAL', 'BORDER', 'BORDERLESS'))
) ENGINE=InnoDB COMMENT='인화 상세';

CREATE TABLE delivery (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    print_order_id  BIGINT          NOT NULL,
    recipient_name  VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    zipcode         VARCHAR(10)     NOT NULL,
    address         VARCHAR(200)    NOT NULL,
    address_detail  VARCHAR(100)    NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    tracking_number VARCHAR(50)     NULL,
    carrier         VARCHAR(50)     NULL,
    delivery_fee    INT UNSIGNED    NOT NULL DEFAULT 0,
    shipped_at      DATETIME        NULL,
    delivered_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_delivery_order (print_order_id),
    INDEX idx_delivery_status (status),
    CONSTRAINT fk_delivery_order FOREIGN KEY (print_order_id) REFERENCES print_order(id),
    CONSTRAINT chk_delivery_status CHECK (status IN ('PENDING', 'PREPARING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED'))
) ENGINE=InnoDB COMMENT='배송';

CREATE TABLE photo_restoration (    -- Vision AI 사용
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    original_url    VARCHAR(500)    NOT NULL,   -- 원본 이미지
    restored_url    VARCHAR(500)    NULL,       -- 복원된 이미지
    mask_data       TEXT            NULL,       -- 마스킹 영역 데이터 (JSON)
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',  -- PENDING, PROCESSING, COMPLETED, FAILED
    -- 토큰 관련
    token_used      INT UNSIGNED    NOT NULL DEFAULT 1,         -- 사용된 토큰 수
    -- 피드백 (AI 품질 개선용)
    feedback_rating VARCHAR(10)     NULL,       -- GOOD, BAD
    feedback_comment VARCHAR(500)   NULL,       -- 피드백 코멘트 (선택)
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_restoration_member (member_id, status),
    CONSTRAINT fk_restoration_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_restoration_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_feedback_rating CHECK (feedback_rating IS NULL OR feedback_rating IN ('GOOD', 'BAD'))
) ENGINE=InnoDB COMMENT='AI 사진 복원';

-- ============================================
-- 5. COMMUNITY
-- ============================================

CREATE TABLE post (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,   -- FK
    photo_lab_id    BIGINT          NULL,       -- FK (현상소 이용 시)
    is_self_developed BOOLEAN       NOT NULL DEFAULT FALSE, -- TRUE: 자가현상, FALSE: 현상소 이용
    title           VARCHAR(200)    NULL,
    content         TEXT            NOT NULL,
    lab_review      VARCHAR(500)    NULL,       -- 현상소 리뷰 (현상소 이용 시)
    like_count      INT UNSIGNED    NOT NULL DEFAULT 0,
    comment_count   INT UNSIGNED    NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_post_member (member_id, status),
    INDEX idx_post_lab (photo_lab_id),
    INDEX idx_post_created (created_at DESC),
    FULLTEXT INDEX ft_post_content (title, content),
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_post_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id),
    CONSTRAINT chk_post_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),
    CONSTRAINT chk_post_lab_required CHECK (
        (is_self_developed = TRUE AND photo_lab_id IS NULL) OR
        (is_self_developed = FALSE AND photo_lab_id IS NOT NULL)
    )
) ENGINE=InnoDB COMMENT='게시글/리뷰';

CREATE TABLE post_image (   -- 1:N
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    post_id         BIGINT          NOT NULL,   -- FK
    image_url       VARCHAR(500)    NOT NULL,   -- GCP Cloud Storage
    display_order   INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_post_image (post_id),
    CONSTRAINT fk_post_image FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='게시글 이미지';

CREATE TABLE comment (  -- 댓글
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    post_id         BIGINT          NOT NULL,
    member_id       BIGINT          NOT NULL,
    content         VARCHAR(1000)   NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_comment_post (post_id, created_at),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id),
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_comment_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED'))
) ENGINE=InnoDB COMMENT='댓글';

CREATE TABLE post_like (    -- 하트 표시
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    post_id         BIGINT          NOT NULL, 
    member_id       BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_like (post_id, member_id),
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB COMMENT='좋아요';

CREATE TABLE favorite_photo_lab (   -- 별 표시
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    photo_lab_id    BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite_lab (member_id, photo_lab_id),
    CONSTRAINT fk_fav_lab_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_fav_lab_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id)
) ENGINE=InnoDB COMMENT='관심 현상소';

-- ============================================
-- 6. INQUIRY
-- ============================================

CREATE TABLE inquiry (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    photo_lab_id    BIGINT          NULL,
    title           VARCHAR(200)    NOT NULL,
    content         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_inquiry_member (member_id, status),
    INDEX idx_inquiry_lab (photo_lab_id),
    CONSTRAINT fk_inquiry_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_inquiry_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id),
    CONSTRAINT chk_inquiry_status CHECK (status IN ('PENDING', 'ANSWERED', 'CLOSED'))
) ENGINE=InnoDB COMMENT='1:1 문의';

CREATE TABLE inquiry_reply (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    inquiry_id      BIGINT          NOT NULL,
    replier_id      BIGINT          NOT NULL,   -- 답변자 (ADMIN: 서비스 문의, OWNER: 현상소 문의)
    content         TEXT            NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_reply_inquiry (inquiry_id),
    CONSTRAINT fk_reply_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry(id),
    CONSTRAINT fk_reply_replier FOREIGN KEY (replier_id) REFERENCES member(id)
) ENGINE=InnoDB COMMENT='문의 답변';

-- ============================================
-- 7. COMMON
-- ============================================
 
CREATE TABLE notice (   -- 전체 회원 공지사항 게시판 
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200)    NOT NULL,
    content         TEXT            NOT NULL,
    notice_type     VARCHAR(20)     NOT NULL DEFAULT 'GENERAL',
    is_pinned       BOOLEAN         NOT NULL DEFAULT FALSE, -- 고정되었는지
    view_count      INT UNSIGNED    NOT NULL DEFAULT 0,     -- 공지에 view count가 필요할까..?
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_notice_pinned (is_pinned DESC, created_at DESC),
    CONSTRAINT chk_notice_type CHECK (notice_type IN ('GENERAL', 'EVENT', 'POLICY'))
) ENGINE=InnoDB COMMENT='공지사항';

CREATE TABLE promotion (    -- 메인페이지 프로모션 배너 부분
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    photo_lab_id    BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    description     VARCHAR(500)    NULL,
    image_url       VARCHAR(500)    NOT NULL,   -- GCP Cloud Storage
    promotion_type  VARCHAR(20)     NOT NULL DEFAULT 'BANNER',
    display_order   INT             NOT NULL DEFAULT 0,
    start_date      DATETIME        NOT NULL,
    end_date        DATETIME        NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_promotion_active (is_active, start_date, end_date),
    CONSTRAINT fk_promotion_lab FOREIGN KEY (photo_lab_id) REFERENCES photo_lab(id),
    CONSTRAINT chk_promotion_type CHECK (promotion_type IN ('BANNER', 'POPUP', 'EVENT'))
) ENGINE=InnoDB COMMENT='프로모션';

CREATE TABLE film_content ( -- 필름 콘텐츠
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200)    NOT NULL,
    subtitle        VARCHAR(300)    NULL,
    content         TEXT            NOT NULL,
    image_url       VARCHAR(500)    NULL,       -- GCP Cloud Storage (썸네일)
    content_type    VARCHAR(20)     NOT NULL DEFAULT 'ARTICLE',
    view_count      INT UNSIGNED    NOT NULL DEFAULT 0,
    is_featured     BOOLEAN         NOT NULL DEFAULT FALSE, -- 메인 페이지 추천 여부
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    INDEX idx_content_featured (is_featured DESC, created_at DESC),
    CONSTRAINT chk_content_type CHECK (content_type IN ('ARTICLE', 'TIP', 'NEWS'))
) ENGINE=InnoDB COMMENT='필름 콘텐츠';

CREATE TABLE notification ( -- 회원별 개인 알림(앱 푸시/ 알림센터)
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    title           VARCHAR(100)    NOT NULL,
    content         VARCHAR(500)    NOT NULL,
    notification_type VARCHAR(20)   NOT NULL,
    related_id      BIGINT          NULL COMMENT '관련 엔티티 ID',
    related_type    VARCHAR(50)     NULL COMMENT '관련 엔티티 타입', 
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_notification_member (member_id, is_read, created_at DESC),
    CONSTRAINT fk_notification_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('ORDER', 'RESERVATION', 'COMMUNITY', 'MARKETING', 'NOTICE'))
) ENGINE=InnoDB COMMENT='알림';

CREATE TABLE payment (  -- 포트원 결제 연동 -- PM에게 확인 필수, 1. 계좌이체 기능 없앰 2. 대신 포트원 카드결제와 간편결제 기능 추가
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    order_type      VARCHAR(20)     NOT NULL,   -- DEVELOPMENT_ORDER, PRINT_ORDER, TOKEN_PURCHASE
    order_id        BIGINT          NULL,       -- 주문 ID (토큰 구매 시 NULL)
    amount          INT UNSIGNED    NOT NULL,   -- 결제 금액 (원)
    token_amount    INT UNSIGNED    NULL,       -- 구매한 토큰 수 (토큰 구매 시)
    payment_method  VARCHAR(20)     NOT NULL,   -- CARD, EASY_PAY, ON_SITE
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- 포트원 PG 연동용
    merchant_uid    VARCHAR(100)    NULL,       -- 주문 고유번호 (우리가 생성)
    imp_uid         VARCHAR(100)    NULL,       -- 포트원 결제 고유번호
    receipt_url     VARCHAR(500)    NULL,       -- 영수증 URL
    -- 결과 정보
    paid_at         DATETIME        NULL,
    fail_reason     VARCHAR(200)    NULL,       -- 결제 실패 사유
    cancelled_at    DATETIME        NULL,
    cancel_reason   VARCHAR(200)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_uid (merchant_uid),
    INDEX idx_payment_member (member_id, status),
    INDEX idx_payment_order (order_type, order_id),
    INDEX idx_payment_imp_uid (imp_uid),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('CARD', 'EASY_PAY', 'ON_SITE')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT chk_order_type CHECK (order_type IN ('DEVELOPMENT_ORDER', 'PRINT_ORDER', 'TOKEN_PURCHASE')),
    CONSTRAINT chk_payment_data CHECK (
        (order_type = 'TOKEN_PURCHASE' AND token_amount IS NOT NULL AND order_id IS NULL) OR
        (order_type IN ('DEVELOPMENT_ORDER', 'PRINT_ORDER') AND order_id IS NOT NULL AND token_amount IS NULL)
    )
) ENGINE=InnoDB COMMENT='결제';
```

---

## 화면-테이블 매핑

| 화면 | 테이블 |
|------|--------|
| CM-020~022 로그인/가입 | member, social_account, member_agreement |
| HM-010 메인 홈 | photo_lab, promotion, film_content |
| HM-021~025 사진 복원 | photo_restoration, token_history, member (token_balance) |
| PL-010~011 현상소 탐색 | photo_lab, photo_lab_keyword |
| PL-020~021 현상소 상세/예약 | photo_lab_*, reservation, payment |
| CO-020~030 사진수다 | post (자가현상 여부), post_image, comment, post_like |
| PM-000~018 현상관리 | development_order, scanned_photo, print_order, print_order_item, delivery, payment |
| UR-010~025 마이페이지 | member, social_account |
| UR-030~040 관심목록 | favorite_photo_lab, post_like |
| UR-060~062 배송지 | member_address |
| UR-070~081 공지/문의 | notice, inquiry, inquiry_reply |
| 알림 센터 | notification |
| 사업자 등록 | photo_lab, photo_lab_document |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|----------|
| 1.0.0 | 2025-12-22 | 최초 작성 (27개 테이블) |
| 1.1.0 | 2025-12-22 | notification, post_hashtag 추가, CHECK 제약 추가, 인덱스 최적화 |
| 1.2.0 | 2025-12-23 | member_agreement, photo_lab_document, photo_restoration, payment 테이블 추가, post.rating, print_order_item.print_method 컬럼 추가 (33개 테이블) |
| 1.2.1 | 2025-12-23 | photo_lab_option → photo_lab_service, reservation_option → reservation_item 테이블명 변경, OptionType → ServiceType Enum명 변경 |
| 1.2.2 | 2025-12-23 | 이미지 테이블 정리: thumbnail_url 제거, width/height 추가 (photo_lab_image, scanned_photo, post_image) |
| 1.2.3 | 2025-12-23 | promotion, film_content에 width/height 추가, film_content.thumbnail_url → image_url 변경 |
| 1.2.4 | 2025-12-23 | NoticeType에 POLICY 추가 (일반공지, 이벤트안내, 약관/정책공지) |
| 1.2.5 | 2025-12-24 | post_hashtag 테이블 제거 (화면설계서 미사용) (32개 테이블) |
| 1.2.6 | 2025-12-24 | payment 테이블 리팩토링: 계좌이체 전용(depositor_name), 포트원 연동 대비(pg_tid, receipt_url), 취소 관련(cancelled_at, cancel_reason) 컬럼 추가 |
| 1.3.0 | 2025-12-24 | **AI 토큰 시스템 추가**: token_history 테이블 신규, member에 token_balance/last_token_refresh_at 추가, photo_restoration에 token_used/feedback_rating/feedback_comment/mask_data 추가, payment에 포트원 필드(merchant_uid, imp_uid, token_amount, fail_reason) 추가 및 TOKEN_PURCHASE 지원 (33개 테이블) |
| 1.3.1 | 2025-12-24 | inquiry_reply.admin_id → replier_id 변경 (ADMIN/OWNER 모두 답변 가능하도록) |
| 1.3.2 | 2025-12-24 | comment 테이블에서 대댓글 기능 제거 (parent_id, idx_comment_parent, fk_comment_parent 삭제) |
| 1.3.3 | 2025-12-24 | favorite_post 테이블 제거 (좋아요한 게시글은 post_like로 조회) (32개 테이블) |
| 1.3.4 | 2025-12-27 | 모든 TINYINT(1) → BOOLEAN 타입으로 변경 (is_default, is_agreed, is_delivery_available, is_main, is_active, is_closed, is_available, is_pinned, is_featured, is_read) |
| 1.4.0 | 2025-12-27 | **스키마 대폭 정리**: social_account에서 토큰 컬럼 제거, reservation_item 테이블 제거, reservation.total_price 제거, post에 is_self_developed 추가 및 rating/view_count 제거, 모든 이미지 테이블에서 width/height 제거 (31개 테이블) |
| 1.4.1 | 2025-12-27 | day_of_week TINYINT → VARCHAR + CHECK 변경, development_order.scan_progress 제거 (scanned_photo COUNT로 계산) |
| 1.4.2 | 2025-12-27 | 오류 수정: member_address FK 누락, photo_lab_bank_account 인덱스 오류, 주석 정리 |
| 1.5.0 | 2025-12-28 | **photo_lab_service 테이블 제거**, reservation에 is_develop/is_scan/is_print 컬럼 추가 (작업 내용 다중 선택), ServiceType Enum 제거 (30개 테이블) |
| 1.5.1 | 2025-12-28 | post 테이블에 lab_review 컬럼 추가 (현상소 이용 시 한줄 리뷰) |
| 1.5.2 | 2025-12-28 | promotion 테이블: link_url 제거, photo_lab_id NOT NULL로 변경 (프로모션은 항상 현상소 상세로 연결) |
| 1.6.0 | 2025-12-28 | **포트원 결제 전환**: photo_lab_bank_account 테이블 제거, payment에서 depositor_name 제거, PaymentMethod를 CARD/EASY_PAY로 변경 (29개 테이블) |
| 1.7.0 | 2025-12-28 | **현장 주문 지원**: photo_lab에 qr_code_url 추가, development_order.reservation_id NULL 허용 + photo_lab_id 추가, print_order.dev_order_id NULL 허용 + photo_lab_id 추가 (예약/현상 없이 주문 가능) |
| 1.8.0 | 2025-12-28 | **스키마 검토 및 정리**: ERD 관계도 현장 주문 반영, comment.status CHECK 추가, promotion.promotion_type CHECK 추가 (BANNER/POPUP/EVENT), print_order_item CHECK 추가 (paper_type/print_method/process), post/inquiry에 photo_lab_id 인덱스 추가 |
| 1.9.0 | 2025-12-28 | **결제 시스템 정리**: development_order에 total_price 추가 (가격 스냅샷), OrderType에서 RESERVATION→DEVELOPMENT_ORDER 변경, PaymentMethod에 ON_SITE 추가 (현장결제) |
| 1.9.1 | 2025-12-28 | payment 테이블에 chk_payment_data CHECK 추가 (TOKEN_PURCHASE↔token_amount, 주문↔order_id 필수 관계 강제) |
| 1.9.2 | 2025-12-28 | post 테이블에 chk_post_lab_required CHECK 추가 (자가현상↔photo_lab_id 일관성 강제) |
