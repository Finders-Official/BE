-- ============================================
-- Finders Mock Data for Development & Demo
-- ============================================
--
-- [사용 방법]
--
-- 1. 로컬 Docker 환경 (처음 시작 또는 데이터 초기화)
--    docker compose down -v   # 기존 볼륨 삭제 (필수!)
--    docker compose up -d     # MySQL 시작 + 자동 실행
--
-- 2. 이미 실행 중인 MySQL에 직접 실행
--    docker exec -i finders-mysql mysql -uroot -proot finders < docker/mysql/init/02_mock_data.sql
--
-- 3. GCP Cloud SQL에 실행
--    gcloud storage cp docker/mysql/init/02_mock_data.sql gs://finders-private/temp/mock_data.sql
--    gcloud sql import sql finders-db gs://finders-private/temp/mock_data.sql --database=finders
--
-- [주의사항]
-- - 실행 시 기존 데이터가 모두 삭제됩니다!
-- - 운영 환경에서는 절대 실행하지 마세요!
--
-- 생성일: 2026-01-19
-- 최종 수정일: 2026-01-19
-- ============================================

USE finders;

-- ============================================
-- 0. 기존 데이터 삭제 (FK 역순)
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- 자식 테이블부터 삭제
TRUNCATE TABLE token_history;
TRUNCATE TABLE payment;
TRUNCATE TABLE inquiry_image;
TRUNCATE TABLE inquiry_reply;
TRUNCATE TABLE inquiry;
TRUNCATE TABLE comments;
TRUNCATE TABLE post_like;
TRUNCATE TABLE post_image;
TRUNCATE TABLE post;
TRUNCATE TABLE photo_restoration;
TRUNCATE TABLE print_order_photo;
TRUNCATE TABLE scanned_photo;
TRUNCATE TABLE print_order_item;
TRUNCATE TABLE delivery;
TRUNCATE TABLE print_order;
TRUNCATE TABLE development_order;
TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_slot;
TRUNCATE TABLE favorite_photo_lab;
TRUNCATE TABLE photo_lab_document;
TRUNCATE TABLE photo_lab_notice;
TRUNCATE TABLE photo_lab_image;
TRUNCATE TABLE photo_lab_business_hour;
TRUNCATE TABLE photo_lab_tag;
TRUNCATE TABLE tag;
TRUNCATE TABLE photo_lab;
TRUNCATE TABLE member_address;
TRUNCATE TABLE member_agreement;
TRUNCATE TABLE social_account;
TRUNCATE TABLE member_user;
TRUNCATE TABLE member_owner;
TRUNCATE TABLE member_admin;
TRUNCATE TABLE member;
TRUNCATE TABLE region;
TRUNCATE TABLE terms;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. Terms (약관)
-- =====================================================
INSERT INTO terms (type, version, title, content, is_required, is_active, effective_date, created_at, updated_at)
VALUES
    ('SERVICE', 'v1.0', '서비스 이용약관', '서비스 이용약관의 상세 내용입니다.', true, true, '2026-01-01', NOW(), NOW()),
    ('PRIVACY', 'v1.0', '개인정보 처리방침', '개인정보 처리방침의 상세 내용입니다.', true, true, '2026-01-01', NOW(), NOW()),
    ('NOTIFICATION', 'v1.0', '알림 수신 동의', '푸시, 문자, 이메일 등 알림 수신 동의 내용입니다.', false, true, '2026-01-01', NOW(), NOW());

-- =====================================================
-- 2. Region (지역)
-- =====================================================
-- 시/도 (sido는 NULL)
INSERT INTO region (sido, sigungu, created_at, updated_at)
VALUES
    (NULL, '서울특별시', NOW(), NOW()),  -- ID 1
    (NULL, '경기도', NOW(), NOW()),      -- ID 2
    (NULL, '부산광역시', NOW(), NOW());  -- ID 3

-- 시/군/구 (sido는 시/도 ID 참조)
INSERT INTO region (sido, sigungu, created_at, updated_at)
VALUES
    (1, '강남구', NOW(), NOW()),        -- ID 4
    (1, '종로구', NOW(), NOW()),        -- ID 5
    (1, '마포구', NOW(), NOW()),        -- ID 6
    (2, '성남시', NOW(), NOW()),        -- ID 7
    (2, '수원시', NOW(), NOW()),        -- ID 8
    (3, '해운대구', NOW(), NOW());      -- ID 9

-- =====================================================
-- 3. Tag (태그)
-- =====================================================
INSERT INTO tag (name, created_at, updated_at)
VALUES
    ('빈티지', NOW(), NOW()),           -- ID 1
    ('흑백', NOW(), NOW()),             -- ID 2
    ('컬러', NOW(), NOW()),             -- ID 3
    ('포토부스', NOW(), NOW()),         -- ID 4
    ('야간촬영', NOW(), NOW()),         -- ID 5
    ('프리미엄', NOW(), NOW());         -- ID 6

-- =====================================================
-- 4. Member (회원)
-- =====================================================
-- JOINED 상속 전략: member 테이블에 먼저 INSERT 후 member_user/member_owner/member_admin에 INSERT

-- 4-1. USER (5명)
-- Member 기본 테이블
INSERT INTO member (role, name, email, phone, status, refresh_token_hash, created_at, updated_at)
VALUES
    ('USER', '김철수', 'user1@test.com', '010-1111-1111', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 1
    ('USER', '이영희', 'user2@test.com', '010-2222-2222', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 2
    ('USER', '박민수', 'user3@test.com', '010-3333-3333', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 3
    ('USER', '최지현', 'user4@test.com', '010-4444-4444', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 4
    ('USER', '정다은', 'user5@test.com', '010-5555-5555', 'ACTIVE', NULL, NOW(), NOW());  -- ID 5

-- MemberUser 테이블
INSERT INTO member_user (member_id, nickname, profile_image, token_balance, last_token_refresh_at, created_at, updated_at)
VALUES
    (1, '철수네', 'users/1/profile.jpg', 3, NULL, NOW(), NOW()),
    (2, '영희야', 'users/2/profile.jpg', 5, NOW(), NOW()),
    (3, '민수왕', 'users/3/profile.jpg', 2, NOW(), NOW()),
    (4, '지현님', NULL, 3, NULL, NOW(), NOW()),
    (5, '다은이', 'users/5/profile.jpg', 10, NOW(), NOW());

-- 4-2. OWNER (3명)
INSERT INTO member (role, name, email, phone, status, refresh_token_hash, created_at, updated_at)
VALUES
    ('OWNER', '홍길동', 'owner1@test.com', '010-6666-6666', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 6
    ('OWNER', '김상현', 'owner2@test.com', '010-7777-7777', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 7
    ('OWNER', '박서준', 'owner3@test.com', '010-8888-8888', 'ACTIVE', NULL, NOW(), NOW());  -- ID 8

-- MemberOwner 테이블
INSERT INTO member_owner (member_id, password_hash, business_number, bank_name, bank_account_number, bank_account_holder, created_at, updated_at)
VALUES
    (6, '$2a$10$dummyhash1', '123-45-67890', '국민은행', '123456789012', '홍길동', NOW(), NOW()),
    (7, '$2a$10$dummyhash2', '234-56-78901', '신한은행', '234567890123', '김상현', NOW(), NOW()),
    (8, '$2a$10$dummyhash3', '345-67-89012', '우리은행', '345678901234', '박서준', NOW(), NOW());

-- 4-3. ADMIN (1명)
INSERT INTO member (role, name, email, phone, status, refresh_token_hash, created_at, updated_at)
VALUES
    ('ADMIN', '관리자', 'admin@finders.com', '010-9999-9999', 'ACTIVE', NULL, NOW(), NOW());  -- ID 9

-- MemberAdmin 테이블
INSERT INTO member_admin (member_id, password_hash, created_at, updated_at)
VALUES
    (9, '$2a$10$dummyadminhash', NOW(), NOW());

-- =====================================================
-- 5. SocialAccount (소셜 계정)
-- =====================================================
INSERT INTO social_account (member_id, provider, provider_id, social_email, created_at, updated_at)
VALUES
    (1, 'KAKAO', 'kakao_1234567890', 'user1@kakao.com', NOW(), NOW()),
    (2, 'GOOGLE', 'google_0987654321', 'user2@gmail.com', NOW(), NOW()),
    (3, 'APPLE', 'apple_1122334455', NULL, NOW(), NOW()),
    (5, 'KAKAO', 'kakao_5566778899', 'user5@kakao.com', NOW(), NOW());

-- =====================================================
-- 6. MemberAgreement (회원 약관 동의)
-- =====================================================
INSERT INTO member_agreement (member_id, terms_id, is_agreed, agreed_at, created_at, updated_at)
VALUES
    -- User 1
    (1, 1, true, NOW(), NOW(), NOW()),
    (1, 2, true, NOW(), NOW(), NOW()),
    (1, 3, false, NOW(), NOW(), NOW()),
    -- User 2
    (2, 1, true, NOW(), NOW(), NOW()),
    (2, 2, true, NOW(), NOW(), NOW()),
    (2, 3, true, NOW(), NOW(), NOW()),
    -- User 3
    (3, 1, true, NOW(), NOW(), NOW()),
    (3, 2, true, NOW(), NOW(), NOW()),
    -- Owner 1
    (6, 1, true, NOW(), NOW(), NOW()),
    (6, 2, true, NOW(), NOW(), NOW());

-- =====================================================
-- 7. MemberAddress (회원 배송지)
-- =====================================================
INSERT INTO member_address (member_id, address_name, zipcode, address, address_detail, is_default, created_at, updated_at)
VALUES
    (1, '집', '06234', '서울특별시 강남구 테헤란로 123', '101동 101호', true, NOW(), NOW()),
    (2, '회사', '03456', '서울특별시 종로구 세종대로 456', '2층', true, NOW(), NOW()),
    (3, '우리집', '13579', '경기도 성남시 분당구 판교역로 789', '201호', true, NOW(), NOW()),
    (5, '본가', '24680', '부산광역시 해운대구 해운대해변로 321', '402호', true, NOW(), NOW());

-- =====================================================
-- 8. PhotoLab (현상소)
-- =====================================================
INSERT INTO photo_lab (
    owner_id, region_id, name, description, phone, zipcode, address, address_detail,
    latitude, longitude, work_count, post_count, reservation_count, avg_work_time,
    status, is_delivery_available, max_reservations_per_hour, qr_code_url,
    created_at, updated_at
)
VALUES
    (6, 4, '빈티지 필름랩', '강남 최고의 빈티지 필름 현상소입니다. 30년 경력의 장인이 직접 현상합니다.', '02-1234-5678', '06234',
     '서울특별시 강남구 테헤란로 123', '지하 1층', 37.50120000, 127.03950000, 15, 3, 8, 120,
     'APPROVED', true, 3, 'https://storage.googleapis.com/finders-qr/lab1.png', NOW(), NOW()),  -- ID 1

    (7, 5, '종로 흑백 스튜디오', '흑백 필름 전문 현상소. 예술가들이 사랑하는 공간입니다.', '02-2345-6789', '03456',
     '서울특별시 종로구 세종대로 456', '3층', 37.57230000, 126.97690000, 8, 2, 5, 90,
     'APPROVED', false, 2, 'https://storage.googleapis.com/finders-qr/lab2.png', NOW(), NOW()),  -- ID 2

    (8, 7, '판교 디지털랩', '최신 장비로 고품질 스캔을 제공합니다. 배송 가능합니다.', '031-3456-7890', '13579',
     '경기도 성남시 분당구 판교역로 789', '1층', 37.39430000, 127.11100000, 22, 3, 12, 60,
     'APPROVED', true, 4, 'https://storage.googleapis.com/finders-qr/lab3.png', NOW(), NOW());  -- ID 3

-- =====================================================
-- 9. PhotoLabTag (현상소-태그 관계)
-- =====================================================
INSERT INTO photo_lab_tag (photo_lab_id, tag_id, created_at, updated_at)
VALUES
    (1, 1, NOW(), NOW()),  -- 빈티지 필름랩 - 빈티지
    (1, 3, NOW(), NOW()),  -- 빈티지 필름랩 - 컬러
    (1, 6, NOW(), NOW()),  -- 빈티지 필름랩 - 프리미엄
    (2, 2, NOW(), NOW()),  -- 종로 흑백 스튜디오 - 흑백
    (2, 1, NOW(), NOW()),  -- 종로 흑백 스튜디오 - 빈티지
    (3, 3, NOW(), NOW()),  -- 판교 디지털랩 - 컬러
    (3, 4, NOW(), NOW());  -- 판교 디지털랩 - 포토부스

-- =====================================================
-- 10. PhotoLabBusinessHour (현상소 영업시간)
-- =====================================================
INSERT INTO photo_lab_business_hour (photo_lab_id, day_of_week, open_time, close_time, is_closed, created_at, updated_at)
VALUES
    -- Lab 1 (월-금 10:00-20:00, 토 11:00-18:00, 일 휴무)
    (1, 'MONDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
    (1, 'TUESDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
    (1, 'WEDNESDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
    (1, 'THURSDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
    (1, 'FRIDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
    (1, 'SATURDAY', '11:00:00', '18:00:00', false, NOW(), NOW()),
    (1, 'SUNDAY', NULL, NULL, true, NOW(), NOW()),
    -- Lab 2 (월-토 09:00-19:00, 일 휴무)
    (2, 'MONDAY', '09:00:00', '19:00:00', false, NOW(), NOW()),
    (2, 'TUESDAY', '09:00:00', '19:00:00', false, NOW(), NOW()),
    (2, 'WEDNESDAY', '09:00:00', '19:00:00', false, NOW(), NOW()),
    (2, 'THURSDAY', '09:00:00', '19:00:00', false, NOW(), NOW()),
    (2, 'FRIDAY', '09:00:00', '19:00:00', false, NOW(), NOW()),
    (2, 'SATURDAY', '09:00:00', '19:00:00', false, NOW(), NOW()),
    (2, 'SUNDAY', NULL, NULL, true, NOW(), NOW()),
    -- Lab 3 (연중무휴 10:00-21:00)
    (3, 'MONDAY', '10:00:00', '21:00:00', false, NOW(), NOW()),
    (3, 'TUESDAY', '10:00:00', '21:00:00', false, NOW(), NOW()),
    (3, 'WEDNESDAY', '10:00:00', '21:00:00', false, NOW(), NOW()),
    (3, 'THURSDAY', '10:00:00', '21:00:00', false, NOW(), NOW()),
    (3, 'FRIDAY', '10:00:00', '21:00:00', false, NOW(), NOW()),
    (3, 'SATURDAY', '10:00:00', '21:00:00', false, NOW(), NOW()),
    (3, 'SUNDAY', '10:00:00', '21:00:00', false, NOW(), NOW());

-- =====================================================
-- 11. PhotoLabImage (현상소 이미지)
-- =====================================================
INSERT INTO photo_lab_image (photo_lab_id, object_path, display_order, is_main, created_at, updated_at)
VALUES
    (1, 'labs/1/main.jpg', 0, true, NOW(), NOW()),
    (1, 'labs/1/interior1.jpg', 1, false, NOW(), NOW()),
    (1, 'labs/1/interior2.jpg', 2, false, NOW(), NOW()),
    (2, 'labs/2/main.jpg', 0, true, NOW(), NOW()),
    (2, 'labs/2/gallery.jpg', 1, false, NOW(), NOW()),
    (3, 'labs/3/main.jpg', 0, true, NOW(), NOW()),
    (3, 'labs/3/equipment.jpg', 1, false, NOW(), NOW()),
    (3, 'labs/3/workspace.jpg', 2, false, NOW(), NOW());

-- =====================================================
-- 12. PhotoLabNotice (현상소 공지사항)
-- =====================================================
INSERT INTO photo_lab_notice (photo_lab_id, title, content, notice_type, start_date, end_date, is_active, created_at, updated_at)
VALUES
    (1, '설 연휴 휴무 안내', '2월 9일~11일은 설 연휴로 휴무합니다.', 'HOLIDAY', '2026-02-09', '2026-02-11', true, NOW(), NOW()),
    (1, '신규 고객 할인 이벤트', '첫 방문 고객 20% 할인!', 'EVENT', '2026-01-01', '2026-01-31', true, NOW(), NOW()),
    (2, '장비 점검 안내', '매주 월요일 오전은 장비 점검으로 예약이 제한됩니다.', 'GENERAL', NULL, NULL, true, NOW(), NOW()),
    (3, '배송비 무료 이벤트', '3만원 이상 구매 시 배송비 무료!', 'EVENT', '2026-01-15', '2026-02-15', true, NOW(), NOW());

-- =====================================================
-- 13. PhotoLabDocument (현상소 서류)
-- =====================================================
INSERT INTO photo_lab_document (photo_lab_id, document_type, object_path, file_name, verified_at, created_at, updated_at)
VALUES
    (1, 'BUSINESS_LICENSE', 'documents/lab1/business_license.pdf', '사업자등록증.pdf', NOW(), NOW(), NOW()),
    (1, 'BANK_ACCOUNT', 'documents/lab1/bank_account.pdf', '통장사본.pdf', NOW(), NOW(), NOW()),
    (2, 'BUSINESS_LICENSE', 'documents/lab2/business_license.pdf', '사업자등록증.pdf', NOW(), NOW(), NOW()),
    (3, 'BUSINESS_LICENSE', 'documents/lab3/business_license.pdf', '사업자등록증.pdf', NOW(), NOW(), NOW()),
    (3, 'BANK_ACCOUNT', 'documents/lab3/bank_account.pdf', '통장사본.pdf', NOW(), NOW(), NOW());

-- =====================================================
-- 14. FavoritePhotoLab (찜한 현상소)
-- =====================================================
INSERT INTO favorite_photo_lab (member_id, photo_lab_id, created_at, updated_at)
VALUES
    (1, 1, NOW(), NOW()),
    (1, 2, NOW(), NOW()),
    (2, 1, NOW(), NOW()),
    (2, 3, NOW(), NOW()),
    (3, 2, NOW(), NOW()),
    (4, 3, NOW(), NOW()),
    (5, 1, NOW(), NOW()),
    (5, 3, NOW(), NOW());

-- =====================================================
-- 15. ReservationSlot (예약 슬롯)
-- =====================================================
INSERT INTO reservation_slot (photo_lab_id, reservation_date, reservation_time, max_capacity, reserved_count, created_at, updated_at)
VALUES
    -- Lab 1 슬롯
    (1, '2026-01-20', '10:00:00', 3, 2, NOW(), NOW()),  -- ID 1
    (1, '2026-01-20', '14:00:00', 3, 1, NOW(), NOW()),  -- ID 2
    (1, '2026-01-21', '11:00:00', 3, 1, NOW(), NOW()),  -- ID 3
    (1, '2026-01-22', '15:00:00', 3, 0, NOW(), NOW()),  -- ID 4
    -- Lab 2 슬롯
    (2, '2026-01-20', '09:00:00', 2, 2, NOW(), NOW()),  -- ID 5
    (2, '2026-01-21', '13:00:00', 2, 1, NOW(), NOW()),  -- ID 6
    (2, '2026-01-22', '10:00:00', 2, 0, NOW(), NOW()),  -- ID 7
    -- Lab 3 슬롯
    (3, '2026-01-20', '12:00:00', 4, 2, NOW(), NOW()),  -- ID 8
    (3, '2026-01-21', '16:00:00', 4, 1, NOW(), NOW()),  -- ID 9
    (3, '2026-01-22', '14:00:00', 4, 0, NOW(), NOW());  -- ID 10

-- =====================================================
-- 16. Reservation (예약) - 10건
-- =====================================================
INSERT INTO reservation (member_id, slot_id, photo_lab_id, status, is_develop, is_scan, is_print, roll_count, request_message, deleted_at, created_at, updated_at)
VALUES
    (1, 1, 1, 'RESERVED', true, true, false, 2, '흑백 필름 2롤 부탁드립니다.', NULL, NOW(), NOW()),       -- ID 1
    (2, 1, 1, 'RESERVED', true, true, true, 1, '컬러 1롤, 스캔과 인화 모두 원합니다.', NULL, NOW(), NOW()),  -- ID 2
    (3, 2, 1, 'RESERVED', true, false, false, 1, '현상만 부탁드립니다.', NULL, NOW(), NOW()),              -- ID 3
    (4, 3, 1, 'COMPLETED', true, true, false, 3, NULL, NULL, NOW(), NOW()),                           -- ID 4
    (1, 5, 2, 'RESERVED', true, true, false, 1, '빈티지 감성으로 부탁드려요.', NULL, NOW(), NOW()),       -- ID 5
    (5, 5, 2, 'RESERVED', false, false, true, 0, '인화만 원합니다.', NULL, NOW(), NOW()),                -- ID 6
    (2, 6, 2, 'CANCELED', true, true, false, 2, '일정 변경으로 취소합니다.', NOW(), NOW(), NOW()),        -- ID 7
    (3, 8, 3, 'RESERVED', true, true, true, 2, '프리미엄 서비스 부탁드립니다.', NULL, NOW(), NOW()),      -- ID 8
    (4, 8, 3, 'RESERVED', true, false, false, 1, NULL, NULL, NOW(), NOW()),                           -- ID 9
    (5, 9, 3, 'COMPLETED', true, true, false, 1, '스캔 해상도 높게 부탁드립니다.', NULL, NOW(), NOW());   -- ID 10

-- =====================================================
-- 17. DevelopmentOrder (현상 주문)
-- =====================================================
INSERT INTO development_order (
    reservation_id, photo_lab_id, member_id, order_code, status, is_develop, is_scan, is_print,
    roll_count, total_photos, total_price, completed_at, created_at, updated_at
)
VALUES
    (1, 1, 1, 'DEV-260120-A1B2C3', 'IN_PROGRESS', true, true, false, 2, 72, 45000, NULL, NOW(), NOW()),       -- ID 1
    (2, 1, 2, 'DEV-260120-D4E5F6', 'RECEIVED', true, true, true, 1, 36, 35000, NULL, NOW(), NOW()),          -- ID 2
    (4, 1, 4, 'DEV-260118-G7H8I9', 'COMPLETED', true, true, false, 3, 108, 60000, NOW(), NOW(), NOW()),      -- ID 3
    (5, 2, 1, 'DEV-260120-J1K2L3', 'IN_PROGRESS', true, true, false, 1, 36, 25000, NULL, NOW(), NOW()),      -- ID 4
    (8, 3, 3, 'DEV-260120-M4N5O6', 'RECEIVED', true, true, true, 2, 72, 55000, NULL, NOW(), NOW()),          -- ID 5
    (10, 3, 5, 'DEV-260119-P7Q8R9', 'COMPLETED', true, true, false, 1, 36, 28000, NOW(), NOW(), NOW());      -- ID 6

-- =====================================================
-- 18. ScannedPhoto (스캔된 사진)
-- =====================================================
INSERT INTO scanned_photo (order_id, image_key, file_name, display_order, created_at, updated_at)
VALUES
    -- Order 3 (completed)
    (3, 'scanned/order3/photo001.jpg', 'photo001.jpg', 0, NOW(), NOW()),
    (3, 'scanned/order3/photo002.jpg', 'photo002.jpg', 1, NOW(), NOW()),
    (3, 'scanned/order3/photo003.jpg', 'photo003.jpg', 2, NOW(), NOW()),
    (3, 'scanned/order3/photo004.jpg', 'photo004.jpg', 3, NOW(), NOW()),
    (3, 'scanned/order3/photo005.jpg', 'photo005.jpg', 4, NOW(), NOW()),
    -- Order 6 (completed)
    (6, 'scanned/order6/photo001.jpg', 'photo001.jpg', 0, NOW(), NOW()),
    (6, 'scanned/order6/photo002.jpg', 'photo002.jpg', 1, NOW(), NOW()),
    (6, 'scanned/order6/photo003.jpg', 'photo003.jpg', 2, NOW(), NOW());

-- =====================================================
-- 19. PhotoRestoration (사진 복원)
-- =====================================================
INSERT INTO photo_restoration (
    member_id, original_path, mask_path, restored_path, restored_width, restored_height,
    status, replicate_prediction_id, token_used, error_message, feedback_rating, feedback_comment,
    created_at, updated_at
)
VALUES
    (2, 'restoration/1/original.jpg', 'restoration/1/mask.png', 'restoration/1/restored.jpg', 1920, 1080,
     'COMPLETED', 'pred_abc123', 1, NULL, 'GOOD', '복원 결과가 만족스럽습니다!', NOW(), NOW()),
    (5, 'restoration/2/original.jpg', 'restoration/2/mask.png', 'restoration/2/restored.jpg', 1920, 1080,
     'COMPLETED', 'pred_def456', 1, NULL, 'EXCELLENT', '정말 잘 복원됐어요!', NOW(), NOW()),
    (3, 'restoration/3/original.jpg', 'restoration/3/mask.png', NULL, NULL, NULL,
     'PROCESSING', 'pred_ghi789', 1, NULL, NULL, NULL, NOW(), NOW());

-- =====================================================
-- 20. Post (게시글) - 8개
-- =====================================================
INSERT INTO post (
    member_id, photo_lab_id, is_self_developed, title, content, lab_review,
    like_count, comment_count, status, created_at, updated_at
)
VALUES
    (1, 1, false, '빈티지 필름랩 후기', '처음 필름 현상을 맡겼는데 정말 만족스러웠습니다!', '친절하고 꼼꼼하게 현상해주셔서 감사합니다.', 5, 3, 'ACTIVE', NOW(), NOW()),  -- ID 1
    (2, NULL, true, '자가 현상 도전기', '집에서 처음으로 흑백 필름 자가 현상에 성공했습니다.', NULL, 12, 5, 'ACTIVE', NOW(), NOW()),  -- ID 2
    (3, 2, false, '흑백 스튜디오 최고!', '예술적인 감성이 살아있는 곳이에요.', '장인 정신이 느껴지는 곳입니다.', 8, 2, 'ACTIVE', NOW(), NOW()),  -- ID 3
    (4, 3, false, '판교 디지털랩 강추', '스캔 품질이 정말 좋아요!', '배송도 빠르고 품질도 훌륭합니다.', 15, 4, 'ACTIVE', NOW(), NOW()),  -- ID 4
    (5, 1, false, '강남 필름 데이트', '데이트 코스로 좋은 곳이에요.', '분위기가 너무 좋아요.', 20, 6, 'ACTIVE', NOW(), NOW()),  -- ID 5
    (1, NULL, true, '필름 카메라 추천', '입문자를 위한 필름 카메라 추천합니다.', NULL, 7, 0, 'ACTIVE', NOW(), NOW()),  -- ID 6
    (2, 2, false, '흑백 필름의 매력', '흑백 필름만의 감성이 있어요.', '현상소 사장님이 정말 친절하셨어요.', 10, 0, 'ACTIVE', NOW(), NOW()),  -- ID 7
    (3, NULL, true, '필름 보관법', '필름을 오래 보관하는 방법을 공유합니다.', NULL, 3, 0, 'ACTIVE', NOW(), NOW());  -- ID 8

-- =====================================================
-- 21. PostImage (게시글 이미지)
-- =====================================================
INSERT INTO post_image (post_id, object_path, display_order, width, height, created_at, updated_at)
VALUES
    (1, 'posts/1/image1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (1, 'posts/1/image2.jpg', 1, 1920, 1080, NOW(), NOW()),
    (2, 'posts/2/image1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (2, 'posts/2/image2.jpg', 1, 1920, 1080, NOW(), NOW()),
    (2, 'posts/2/image3.jpg', 2, 1920, 1080, NOW(), NOW()),
    (3, 'posts/3/image1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (4, 'posts/4/image1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (4, 'posts/4/image2.jpg', 1, 1920, 1080, NOW(), NOW()),
    (5, 'posts/5/image1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (5, 'posts/5/image2.jpg', 1, 1920, 1080, NOW(), NOW()),
    (5, 'posts/5/image3.jpg', 2, 1920, 1080, NOW(), NOW()),
    (5, 'posts/5/image4.jpg', 3, 1920, 1080, NOW(), NOW());

-- =====================================================
-- 22. PostLike (게시글 좋아요)
-- =====================================================
INSERT INTO post_like (post_id, member_id, created_at, updated_at)
VALUES
    (1, 2, NOW(), NOW()),
    (1, 3, NOW(), NOW()),
    (1, 4, NOW(), NOW()),
    (1, 5, NOW(), NOW()),
    (1, 1, NOW(), NOW()),
    (2, 1, NOW(), NOW()),
    (2, 3, NOW(), NOW()),
    (2, 4, NOW(), NOW()),
    (2, 5, NOW(), NOW()),
    (3, 1, NOW(), NOW()),
    (3, 2, NOW(), NOW()),
    (4, 1, NOW(), NOW()),
    (4, 2, NOW(), NOW()),
    (4, 3, NOW(), NOW()),
    (5, 1, NOW(), NOW()),
    (5, 2, NOW(), NOW()),
    (5, 3, NOW(), NOW()),
    (5, 4, NOW(), NOW());

-- =====================================================
-- 23. Comments (댓글) - 20개
-- =====================================================
INSERT INTO comments (post_id, member_id, content, status, created_at, updated_at)
VALUES
    (1, 2, '저도 가보고 싶네요!', 'ACTIVE', NOW(), NOW()),
    (1, 3, '사진 정말 잘 나왔어요', 'ACTIVE', NOW(), NOW()),
    (1, 4, '다음에 저도 가봐야겠어요', 'ACTIVE', NOW(), NOW()),
    (2, 1, '자가 현상 정말 대단하시네요!', 'ACTIVE', NOW(), NOW()),
    (2, 3, '저도 도전해보고 싶어요', 'ACTIVE', NOW(), NOW()),
    (2, 4, '팁 공유해주실 수 있나요?', 'ACTIVE', NOW(), NOW()),
    (2, 5, '와 정말 멋지네요!', 'ACTIVE', NOW(), NOW()),
    (2, 1, '감사합니다!', 'ACTIVE', NOW(), NOW()),
    (3, 1, '저도 흑백 필름 좋아해요', 'ACTIVE', NOW(), NOW()),
    (3, 2, '분위기가 정말 좋아보여요', 'ACTIVE', NOW(), NOW()),
    (4, 1, '스캔 품질 진짜 좋더라구요', 'ACTIVE', NOW(), NOW()),
    (4, 2, '배송도 빠르고 좋아요', 'ACTIVE', NOW(), NOW()),
    (4, 3, '가격도 합리적이에요', 'ACTIVE', NOW(), NOW()),
    (4, 5, '추천 감사합니다!', 'ACTIVE', NOW(), NOW()),
    (5, 1, '데이트 코스로 딱이네요', 'ACTIVE', NOW(), NOW()),
    (5, 2, '분위기 진짜 좋아요', 'ACTIVE', NOW(), NOW()),
    (5, 3, '주말에 가봐야겠어요', 'ACTIVE', NOW(), NOW()),
    (5, 4, '저도 다음에 가볼게요', 'ACTIVE', NOW(), NOW()),
    (5, 1, '사진 찍기 좋은 곳이에요', 'ACTIVE', NOW(), NOW()),
    (5, 2, '필름 감성 느끼기 좋아요', 'ACTIVE', NOW(), NOW());

-- =====================================================
-- 24. Inquiry (문의)
-- =====================================================
INSERT INTO inquiry (member_id, photo_lab_id, title, content, status, created_at, updated_at)
VALUES
    (1, 1, '예약 변경 가능한가요?', '이번주 금요일 예약을 토요일로 변경하고 싶습니다.', 'ANSWERED', NOW(), NOW()),  -- ID 1
    (2, NULL, '회원 탈퇴 문의', '회원 탈퇴는 어떻게 하나요?', 'ANSWERED', NOW(), NOW()),  -- ID 2
    (3, 2, '흑백 필름 가격 문의', '흑백 필름 현상 가격이 궁금합니다.', 'PENDING', NOW(), NOW()),  -- ID 3
    (4, 3, '배송 기간 문의', '스캔 후 배송은 얼마나 걸리나요?', 'ANSWERED', NOW(), NOW());  -- ID 4

-- =====================================================
-- 25. InquiryReply (문의 답변)
-- =====================================================
INSERT INTO inquiry_reply (inquiry_id, replier_id, content, created_at, updated_at)
VALUES
    (1, 6, '예약 변경 가능합니다. 전화 주시면 바로 처리해드리겠습니다.', NOW(), NOW()),
    (2, 9, '설정 > 계정 관리 > 회원 탈퇴에서 진행하실 수 있습니다.', NOW(), NOW()),
    (4, 8, '스캔 완료 후 1-2일 이내 배송됩니다.', NOW(), NOW());

-- =====================================================
-- 26. InquiryImage (문의 이미지)
-- =====================================================
INSERT INTO inquiry_image (inquiry_id, object_path, display_order, created_at, updated_at)
VALUES
    (1, 'inquiries/1/screenshot.jpg', 0, NOW(), NOW()),
    (3, 'inquiries/3/film_sample.jpg', 0, NOW(), NOW());

-- =====================================================
-- 27. Payment (결제)
-- =====================================================
INSERT INTO payment (
    member_id, order_type, related_order_id, payment_id, order_name, amount, token_amount,
    transaction_id, pg_tx_id, pg_provider, method, status, card_company, card_number,
    approve_no, installment_months, receipt_url, requested_at, paid_at,
    fail_code, fail_message, cancelled_at, cancel_reason, cancel_amount,
    created_at, updated_at
)
VALUES
    (1, 'DEVELOPMENT', 1, 'pay_260120_A1B2C3D4E5F6', '필름 현상 2롤', 45000, NULL,
     'tx_ABC123456', 'pg_DEF789', 'TOSSPAYMENTS', 'CARD', 'PAID', '신한카드', '1234-****-****-5678',
     '12345678', 0, 'https://receipt.example.com/1', NOW(), NOW(),
     NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),  -- ID 1

    (2, 'DEVELOPMENT', 2, 'pay_260120_B2C3D4E5F6G7', '필름 현상 1롤 + 인화', 35000, NULL,
     NULL, NULL, NULL, NULL, 'READY', NULL, NULL,
     NULL, NULL, NULL, NOW(), NULL,
     NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),  -- ID 2

    (5, 'TOKEN', NULL, 'pay_260120_C3D4E5F6G7H8', '토큰 10개 구매', 10000, 10,
     'tx_GHI123456', 'pg_JKL789', 'TOSSPAYMENTS', 'CARD', 'PAID', '국민카드', '9876-****-****-4321',
     '87654321', 0, 'https://receipt.example.com/3', NOW(), NOW(),
     NULL, NULL, NULL, NULL, NULL, NOW(), NOW());  -- ID 3

-- =====================================================
-- 28. TokenHistory (토큰 사용 내역)
-- =====================================================
INSERT INTO token_history (
    member_id, type, amount, balance_after, related_type, related_id, description,
    created_at, updated_at
)
VALUES
    (2, 'USE', -1, 4, 'PHOTO_RESTORATION', 1, '사진 복원 사용', NOW(), NOW()),
    (5, 'USE', -1, 9, 'PHOTO_RESTORATION', 2, '사진 복원 사용', NOW(), NOW()),
    (5, 'PURCHASE', 10, 19, 'PAYMENT', 3, '토큰 10개 구매', NOW(), NOW()),
    (5, 'USE', -1, 8, 'PHOTO_RESTORATION', 3, '사진 복원 사용', NOW(), NOW()),
    (3, 'USE', -1, 1, 'PHOTO_RESTORATION', NULL, '사진 복원 사용', NOW(), NOW());

-- =====================================================
-- Mock Data 작성 완료
-- =====================================================
-- 회원: 9명 (USER 5, OWNER 3, ADMIN 1)
-- 현상소: 3개
-- 예약: 10건
-- 게시글: 8개
-- 댓글: 20개
-- =====================================================
