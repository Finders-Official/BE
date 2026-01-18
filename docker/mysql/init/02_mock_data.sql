-- =====================================================
-- Finders Mock Data SQL
-- =====================================================
-- 새로운 ERD 구조에 맞춘 Mock 데이터
-- JOINED 상속 전략 반영 (Member → MemberUser/MemberOwner/MemberAdmin)
-- =====================================================

-- [사용 방법]
-- 1. 로컬 Docker 환경: docker compose down -v && docker compose up -d
-- 2. 직접 실행: docker exec -i finders-mysql mysql -uroot -proot finders < docker/mysql/init/02_mock_data.sql
-- 3. GCP Cloud SQL: gcloud storage cp docker/mysql/init/02_mock_data.sql gs://finders-private/temp/ && gcloud sql import sql finders-db gs://finders-private/temp/02_mock_data.sql --database=finders

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 0. 기존 데이터 초기화 (FK 순서대로 역순 삭제)
-- =====================================================
TRUNCATE TABLE payment;
TRUNCATE TABLE inquiry_reply;
TRUNCATE TABLE inquiry_image;
TRUNCATE TABLE inquiry;
TRUNCATE TABLE post_like;
TRUNCATE TABLE comments;
TRUNCATE TABLE post_image;
TRUNCATE TABLE post;
TRUNCATE TABLE photo_restoration;
TRUNCATE TABLE delivery;
TRUNCATE TABLE print_order_photo;
TRUNCATE TABLE print_order_item;
TRUNCATE TABLE print_order;
TRUNCATE TABLE scanned_photo;
TRUNCATE TABLE development_order;
TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_slot;
TRUNCATE TABLE favorite_photo_lab;
TRUNCATE TABLE token_history;
TRUNCATE TABLE member_agreement;
TRUNCATE TABLE terms;
TRUNCATE TABLE member_address;
TRUNCATE TABLE social_account;
TRUNCATE TABLE photo_lab_document;
TRUNCATE TABLE photo_lab_notice;
TRUNCATE TABLE photo_lab_business_hour;
TRUNCATE TABLE photo_lab_tag;
TRUNCATE TABLE photo_lab_image;
TRUNCATE TABLE photo_lab;
TRUNCATE TABLE tag;
TRUNCATE TABLE region;
TRUNCATE TABLE member_user;
TRUNCATE TABLE member_owner;
TRUNCATE TABLE member_admin;
TRUNCATE TABLE member;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. Terms (약관) - 3개
-- =====================================================
INSERT INTO terms (type, version, title, content, is_required, is_active, effective_date, created_at, updated_at)
VALUES
    ('SERVICE', 'v1.0', '서비스 이용약관', '파인더스 서비스 이용약관입니다...', true, true, '2026-01-01', NOW(), NOW()),
    ('PRIVACY', 'v1.0', '개인정보 처리방침', '파인더스 개인정보 처리방침입니다...', true, true, '2026-01-01', NOW(), NOW()),
    ('NOTIFICATION', 'v1.0', '알림 수신 동의', '파인더스 알림 수신에 동의합니다...', false, true, '2026-01-01', NOW(), NOW());

-- =====================================================
-- 2. Region (지역) - 시/도 3개 + 시/군/구 6개
-- =====================================================
-- 시/도 먼저 (sido NULL)
INSERT INTO region (sigungu, sido, created_at, updated_at)
VALUES
    ('서울', NULL, NOW(), NOW()),  -- ID 1
    ('경기', NULL, NOW(), NOW()),  -- ID 2
    ('부산', NULL, NOW(), NOW());  -- ID 3

-- 시/군/구 (sido FK)
INSERT INTO region (sigungu, sido, created_at, updated_at)
VALUES
    ('강남구', 1, NOW(), NOW()),   -- ID 4
    ('마포구', 1, NOW(), NOW()),   -- ID 5
    ('수원시', 2, NOW(), NOW()),   -- ID 6
    ('성남시', 2, NOW(), NOW()),   -- ID 7
    ('해운대구', 3, NOW(), NOW()), -- ID 8
    ('부산진구', 3, NOW(), NOW()); -- ID 9

-- =====================================================
-- 3. Tag (현상소 태그) - 6개
-- =====================================================
INSERT INTO tag (name, created_at, updated_at)
VALUES
    ('흑백전문', NOW(), NOW()),
    ('칼라전문', NOW(), NOW()),
    ('빠른현상', NOW(), NOW()),
    ('친절한상담', NOW(), NOW()),
    ('주차가능', NOW(), NOW()),
    ('배송가능', NOW(), NOW());

-- =====================================================
-- 4. Member (JOINED 상속) - 9명
-- =====================================================

-- 4-1. USER (5명)
-- member 테이블에 먼저 INSERT
INSERT INTO member (role, name, email, phone, status, refresh_token_hash, created_at, updated_at)
VALUES
    ('USER', '김철수', 'user1@test.com', '010-1111-1111', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 1
    ('USER', '이영희', 'user2@test.com', '010-2222-2222', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 2
    ('USER', '박민수', 'user3@test.com', '010-3333-3333', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 3
    ('USER', '최지현', 'user4@test.com', '010-4444-4444', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 4
    ('USER', '정다은', 'user5@test.com', '010-5555-5555', 'ACTIVE', NULL, NOW(), NOW());  -- ID 5

-- member_user 테이블에 INSERT (created_at, updated_at 없음!)
INSERT INTO member_user (member_id, nickname, profile_image, token_balance, last_token_refresh_at)
VALUES
    (1, '철수네', 'profiles/1/profile.jpg', 3, NULL),
    (2, '영희야', 'profiles/2/profile.jpg', 5, NOW()),
    (3, '민수왕', 'profiles/3/profile.jpg', 2, NOW()),
    (4, '지현님', NULL, 3, NULL),
    (5, '다은이', 'profiles/5/profile.jpg', 10, NOW());

-- 4-2. OWNER (3명)
INSERT INTO member (role, name, email, phone, status, refresh_token_hash, created_at, updated_at)
VALUES
    ('OWNER', '홍길동', 'owner1@test.com', '010-6666-6666', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 6
    ('OWNER', '김상현', 'owner2@test.com', '010-7777-7777', 'ACTIVE', NULL, NOW(), NOW()),  -- ID 7
    ('OWNER', '박서준', 'owner3@test.com', '010-8888-8888', 'ACTIVE', NULL, NOW(), NOW());  -- ID 8

-- member_owner 테이블에 INSERT (created_at, updated_at 없음!)
INSERT INTO member_owner (member_id, password_hash, business_number, bank_name, bank_account_number, bank_account_holder)
VALUES
    (6, '$2a$10$dummyhash1', '123-45-67890', '국민은행', '123456789012', '홍길동'),
    (7, '$2a$10$dummyhash2', '234-56-78901', '신한은행', '234567890123', '김상현'),
    (8, '$2a$10$dummyhash3', '345-67-89012', '우리은행', '345678901234', '박서준');

-- 4-3. ADMIN (1명)
INSERT INTO member (role, name, email, phone, status, refresh_token_hash, created_at, updated_at)
VALUES
    ('ADMIN', '관리자', 'admin@finders.com', '010-9999-9999', 'ACTIVE', NULL, NOW(), NOW());  -- ID 9

-- member_admin 테이블에 INSERT (created_at, updated_at 없음!)
INSERT INTO member_admin (member_id, password_hash)
VALUES
    (9, '$2a$10$dummyadminhash');

-- =====================================================
-- 5. SocialAccount (소셜 계정 - User 전용)
-- =====================================================
INSERT INTO social_account (member_id, provider, provider_id, social_email, created_at, updated_at)
VALUES
    (1, 'KAKAO', 'kakao_1234567890', 'user1@kakao.com', NOW(), NOW()),
    (2, 'APPLE', 'apple_0987654321', NULL, NOW(), NOW()),
    (3, 'KAKAO', 'kakao_1122334455', 'user3@kakao.com', NOW(), NOW()),
    (5, 'APPLE', 'apple_5566778899', NULL, NOW(), NOW());

-- =====================================================
-- 6. MemberAgreement (회원 약관 동의) - camelCase 컬럼명 주의!
-- =====================================================
INSERT INTO member_agreement (member_id, terms_id, isAgreed, agreedAt, created_at, updated_at)
VALUES
    -- User 1
    (1, 1, true, NOW(), NOW(), NOW()),
    (1, 2, true, NOW(), NOW(), NOW()),
    (1, 3, false, NOW(), NOW(), NOW()),
    -- User 2
    (2, 1, true, NOW(), NOW(), NOW()),
    (2, 2, true, NOW(), NOW(), NOW()),
    (2, 3, true, NOW(), NOW(), NOW()),
    -- Owner 6
    (6, 1, true, NOW(), NOW(), NOW()),
    (6, 2, true, NOW(), NOW(), NOW());

-- =====================================================
-- 7. MemberAddress (배송지 - User 전용)
-- =====================================================
INSERT INTO member_address (member_id, address_name, zipcode, address, address_detail, is_default, created_at, updated_at)
VALUES
    (1, '집', '06234', '서울 강남구 테헤란로 123', '101동 1001호', true, NOW(), NOW()),
    (2, '회사', '03925', '서울 마포구 월드컵북로 456', '3층', true, NOW(), NOW()),
    (3, '집', '16514', '경기 수원시 영통구 광교로 789', '201호', true, NOW(), NOW());

-- =====================================================
-- 8. TokenHistory (토큰 내역 - User 전용)
-- =====================================================
INSERT INTO token_history (member_id, type, amount, balance_after, related_type, related_id, description, created_at, updated_at)
VALUES
    (1, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스', NOW(), NOW()),
    (2, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스', NOW(), NOW()),
    (2, 'PURCHASE', 2, 5, NULL, NULL, '토큰 구매', NOW(), NOW()),
    (3, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스', NOW(), NOW()),
    (3, 'USE', -1, 2, 'PHOTO_RESTORATION', 1, 'AI 복원 사용', NOW(), NOW()),
    (5, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스', NOW(), NOW()),
    (5, 'PURCHASE', 7, 10, NULL, NULL, '토큰 구매', NOW(), NOW());

-- =====================================================
-- 9. PhotoLab (현상소) - 3개
-- =====================================================
INSERT INTO photo_lab (
    owner_id, region_id, name, description, phone,
    zipcode, address, address_detail,
    latitude, longitude,
    work_count, post_count, reservation_count, avg_work_time,
    status, is_delivery_available, max_reservations_per_hour, qr_code_url, created_at, updated_at
)
VALUES
    (6, 4, '강남 필름 현상소', '강남 최고의 필름 현상소입니다', '02-1234-5678',
     '06234', '서울 강남구 테헤란로 123', '1층',
     37.49794708, 127.02762427,
     150, 30, 200, 120,
     'ACTIVE', true, 3, 'photo-labs/1/qr.png', NOW(), NOW()),

    (7, 5, '마포 아날로그 스튜디오', '마포의 감성 필름 현상소', '02-2345-6789',
     '03925', '서울 마포구 월드컵북로 456', '지하1층',
     37.55738567, 126.93694635,
     80, 15, 120, 150,
     'ACTIVE', false, 2, 'photo-labs/2/qr.png', NOW(), NOW()),

    (8, 8, '해운대 필름 연구소', '부산 최고의 필름 현상', '051-3456-7890',
     '48094', '부산 해운대구 해운대해변로 298', '2층',
     35.15869564, 129.16035635,
     200, 50, 300, 100,
     'ACTIVE', true, 4, 'photo-labs/3/qr.png', NOW(), NOW());

-- =====================================================
-- 10. PhotoLabImage (현상소 이미지)
-- =====================================================
INSERT INTO photo_lab_image (photo_lab_id, object_path, display_order, is_main, created_at, updated_at)
VALUES
    (1, 'photo-labs/1/images/main.jpg', 0, true, NOW(), NOW()),
    (1, 'photo-labs/1/images/interior1.jpg', 1, false, NOW(), NOW()),
    (1, 'photo-labs/1/images/interior2.jpg', 2, false, NOW(), NOW()),
    (2, 'photo-labs/2/images/main.jpg', 0, true, NOW(), NOW()),
    (2, 'photo-labs/2/images/work1.jpg', 1, false, NOW(), NOW()),
    (3, 'photo-labs/3/images/main.jpg', 0, true, NOW(), NOW()),
    (3, 'photo-labs/3/images/exterior.jpg', 1, false, NOW(), NOW());

-- =====================================================
-- 11. PhotoLabTag (현상소-태그 매핑)
-- =====================================================
INSERT INTO photo_lab_tag (photo_lab_id, tag_id, created_at, updated_at)
VALUES
    (1, 1, NOW(), NOW()), -- 강남: 흑백전문
    (1, 3, NOW(), NOW()), -- 강남: 빠른현상
    (1, 6, NOW(), NOW()), -- 강남: 배송가능
    (2, 2, NOW(), NOW()), -- 마포: 칼라전문
    (2, 4, NOW(), NOW()), -- 마포: 친절한상담
    (3, 1, NOW(), NOW()), -- 해운대: 흑백전문
    (3, 2, NOW(), NOW()), -- 해운대: 칼라전문
    (3, 5, NOW(), NOW()), -- 해운대: 주차가능
    (3, 6, NOW(), NOW()); -- 해운대: 배송가능

-- =====================================================
-- 12. PhotoLabBusinessHour (영업시간)
-- =====================================================
INSERT INTO photo_lab_business_hour (photo_lab_id, day_of_week, open_time, close_time, is_closed, created_at, updated_at)
VALUES
    -- 강남 필름 (월~금 운영, 주말 휴무)
    (1, 'MONDAY', '10:00:00', '19:00:00', false, NOW(), NOW()),
    (1, 'TUESDAY', '10:00:00', '19:00:00', false, NOW(), NOW()),
    (1, 'WEDNESDAY', '10:00:00', '19:00:00', false, NOW(), NOW()),
    (1, 'THURSDAY', '10:00:00', '19:00:00', false, NOW(), NOW()),
    (1, 'FRIDAY', '10:00:00', '19:00:00', false, NOW(), NOW()),
    (1, 'SATURDAY', NULL, NULL, true, NOW(), NOW()),
    (1, 'SUNDAY', NULL, NULL, true, NOW(), NOW()),

    -- 마포 스튜디오 (화~토 운영)
    (2, 'MONDAY', NULL, NULL, true, NOW(), NOW()),
    (2, 'TUESDAY', '11:00:00', '20:00:00', false, NOW(), NOW()),
    (2, 'WEDNESDAY', '11:00:00', '20:00:00', false, NOW(), NOW()),
    (2, 'THURSDAY', '11:00:00', '20:00:00', false, NOW(), NOW()),
    (2, 'FRIDAY', '11:00:00', '20:00:00', false, NOW(), NOW()),
    (2, 'SATURDAY', '11:00:00', '18:00:00', false, NOW(), NOW()),
    (2, 'SUNDAY', NULL, NULL, true, NOW(), NOW()),

    -- 해운대 연구소 (매일 운영)
    (3, 'MONDAY', '09:00:00', '20:00:00', false, NOW(), NOW()),
    (3, 'TUESDAY', '09:00:00', '20:00:00', false, NOW(), NOW()),
    (3, 'WEDNESDAY', '09:00:00', '20:00:00', false, NOW(), NOW()),
    (3, 'THURSDAY', '09:00:00', '20:00:00', false, NOW(), NOW()),
    (3, 'FRIDAY', '09:00:00', '20:00:00', false, NOW(), NOW()),
    (3, 'SATURDAY', '10:00:00', '18:00:00', false, NOW(), NOW()),
    (3, 'SUNDAY', '10:00:00', '18:00:00', false, NOW(), NOW());

-- =====================================================
-- 13. PhotoLabNotice (현상소 공지사항)
-- =====================================================
INSERT INTO photo_lab_notice (photo_lab_id, title, content, notice_type, start_date, end_date, is_active, created_at, updated_at)
VALUES
    (1, '연휴 휴무 안내', '설 연휴 기간 휴무입니다', 'GENERAL', '2026-02-01', '2026-02-05', true, NOW(), NOW()),
    (2, '신규 오픈 할인 이벤트', '20% 할인 이벤트 진행중', 'EVENT', '2026-01-01', '2026-02-28', true, NOW(), NOW()),
    (3, '주차장 이용 안내', '주차장 공사로 임시 주차장 이용 부탁드립니다', 'GENERAL', NULL, NULL, true, NOW(), NOW());

-- =====================================================
-- 14. PhotoLabDocument (사업자 증빙 서류)
-- =====================================================
INSERT INTO photo_lab_document (photo_lab_id, document_type, object_path, file_name, verified_at, created_at, updated_at)
VALUES
    (1, 'BUSINESS_LICENSE', 'photo-labs/1/documents/BUSINESS_LICENSE/license.pdf', '사업자등록증.pdf', NOW(), NOW(), NOW()),
    (2, 'BUSINESS_LICENSE', 'photo-labs/2/documents/BUSINESS_LICENSE/license.pdf', '사업자등록증.pdf', NOW(), NOW(), NOW()),
    (3, 'BUSINESS_LICENSE', 'photo-labs/3/documents/BUSINESS_LICENSE/license.pdf', '사업자등록증.pdf', NOW(), NOW(), NOW());

-- =====================================================
-- 15. FavoritePhotoLab (관심 현상소)
-- =====================================================
INSERT INTO favorite_photo_lab (member_id, photo_lab_id, created_at, updated_at)
VALUES
    (1, 1, NOW(), NOW()),
    (1, 2, NOW(), NOW()),
    (2, 1, NOW(), NOW()),
    (2, 3, NOW(), NOW()),
    (3, 2, NOW(), NOW()),
    (5, 1, NOW(), NOW()),
    (5, 3, NOW(), NOW());

-- =====================================================
-- 16. ReservationSlot (예약 슬롯)
-- =====================================================
INSERT INTO reservation_slot (photo_lab_id, reservation_date, reservation_time, max_capacity, reserved_count, created_at, updated_at)
VALUES
    -- 강남 (2026-01-20)
    (1, '2026-01-20', '10:00:00', 3, 2, NOW(), NOW()),
    (1, '2026-01-20', '14:00:00', 3, 1, NOW(), NOW()),
    (1, '2026-01-20', '16:00:00', 3, 0, NOW(), NOW()),
    -- 마포 (2026-01-21)
    (2, '2026-01-21', '11:00:00', 2, 1, NOW(), NOW()),
    (2, '2026-01-21', '15:00:00', 2, 2, NOW(), NOW()),
    -- 해운대 (2026-01-22)
    (3, '2026-01-22', '10:00:00', 4, 3, NOW(), NOW()),
    (3, '2026-01-22', '14:00:00', 4, 1, NOW(), NOW());

-- =====================================================
-- 17. Reservation (예약)
-- =====================================================
INSERT INTO reservation (member_id, slot_id, photo_lab_id, status, is_develop, is_scan, is_print, roll_count, request_message, created_at, updated_at)
VALUES
    (1, 1, 1, 'CONFIRMED', true, true, false, 2, '흑백 필름 2롤 현상 부탁드립니다', NOW(), NOW()),
    (2, 1, 1, 'CONFIRMED', true, true, true, 1, '칼라 필름 1롤 현상 후 인화까지', NOW(), NOW()),
    (2, 4, 2, 'CONFIRMED', true, true, false, 1, NULL, NOW(), NOW()),
    (3, 5, 2, 'PENDING', true, false, false, 1, '현상만 부탁드립니다', NOW(), NOW()),
    (5, 5, 2, 'PENDING', true, true, true, 3, '3롤 현상+스캔+인화', NOW(), NOW()),
    (1, 6, 3, 'CONFIRMED', true, true, false, 1, NULL, NOW(), NOW()),
    (2, 6, 3, 'CONFIRMED', true, true, false, 2, NULL, NOW(), NOW()),
    (3, 6, 3, 'CONFIRMED', true, false, false, 1, '현상만', NOW(), NOW()),
    (4, 7, 3, 'PENDING', true, true, true, 1, '전체 서비스', NOW(), NOW()),
    (5, 2, 1, 'CANCELLED', true, true, false, 1, '취소된 예약', NOW(), NOW());

-- =====================================================
-- 18. DevelopmentOrder (현상 주문)
-- =====================================================
INSERT INTO development_order (
    reservation_id, photo_lab_id, member_id, order_code, status,
    is_develop, is_scan, is_print, roll_count, total_photos, total_price, completed_at, created_at, updated_at
)
VALUES
    (1, 1, 1, 'DEV20260115001', 'COMPLETED', true, true, false, 2, 48, 30000, NOW(), NOW(), NOW()),
    (2, 1, 2, 'DEV20260116001', 'SCANNING', true, true, true, 1, 24, 35000, NULL, NOW(), NOW()),
    (3, 2, 2, 'DEV20260117001', 'DEVELOPING', true, true, false, 1, 0, 15000, NULL, NOW(), NOW()),
    (6, 3, 1, 'DEV20260118001', 'RECEIVED', true, true, false, 1, 0, 15000, NULL, NOW(), NOW());

-- =====================================================
-- 19. ScannedPhoto (스캔된 사진) - image_key 컬럼명 주의!
-- =====================================================
INSERT INTO scanned_photo (order_id, image_key, file_name, display_order, created_at, updated_at)
VALUES
    (1, 'temp/orders/1/scans/001.jpg', '001.jpg', 1, NOW(), NOW()),
    (1, 'temp/orders/1/scans/002.jpg', '002.jpg', 2, NOW(), NOW()),
    (1, 'temp/orders/1/scans/003.jpg', '003.jpg', 3, NOW(), NOW()),
    (2, 'temp/orders/2/scans/001.jpg', '001.jpg', 1, NOW(), NOW()),
    (2, 'temp/orders/2/scans/002.jpg', '002.jpg', 2, NOW(), NOW());

-- =====================================================
-- 20. PrintOrder (인화 주문)
-- =====================================================
INSERT INTO print_order (
    dev_order_id, photo_lab_id, member_id, order_code, status, total_price,
    receipt_method, estimated_at, completed_at, created_at, updated_at
)
VALUES
    (2, 1, 2, 'PRT20260116001', 'CONFIRMED', 25000, 'DELIVERY', '2026-01-23 14:00:00', NULL, NOW(), NOW()),
    (NULL, 3, 5, 'PRT20260117001', 'PENDING', 15000, 'PICKUP', NULL, NULL, NOW(), NOW());

-- =====================================================
-- 21. PrintOrderItem (인화 상세)
-- =====================================================
INSERT INTO print_order_item (
    print_order_id, film_type, paper_type, print_method, size, frame_type, unit_price, total_price, created_at, updated_at
)
VALUES
    (1, 'COLOR_NEGATIVE', 'GLOSSY', 'INKJET', 'SIZE_4X6', 'NONE', 500, 12000, NOW(), NOW()),
    (2, 'COLOR_NEGATIVE', 'MATTE', 'INKJET', 'SIZE_5X7', 'NONE', 800, 16000, NOW(), NOW());

-- =====================================================
-- 22. PrintOrderPhoto (인화 주문 사진 매핑)
-- =====================================================
INSERT INTO print_order_photo (print_order_id, scanned_photo_id, quantity, created_at, updated_at)
VALUES
    (1, 4, 24, NOW(), NOW()),
    (1, 5, 24, NOW(), NOW());

-- =====================================================
-- 23. Delivery (배송)
-- =====================================================
INSERT INTO delivery (
    print_order_id, recipient_name, phone, zipcode, address, address_detail,
    status, tracking_number, carrier, delivery_fee, shipped_at, delivered_at, created_at, updated_at
)
VALUES
    (1, '이영희', '010-2222-2222', '03925', '서울 마포구 월드컵북로 456', '3층',
     'PREPARING', NULL, NULL, 3000, NULL, NULL, NOW(), NOW());

-- =====================================================
-- 24. PhotoRestoration (AI 사진 복원)
-- =====================================================
INSERT INTO photo_restoration (
    member_id, original_path, mask_path, restored_path, restored_width, restored_height,
    status, replicate_prediction_id, token_used, error_message, feedback_rating, feedback_comment, created_at, updated_at
)
VALUES
    (3, 'restorations/3/original/photo1.jpg', 'restorations/3/mask/photo1.jpg',
     'restorations/3/restored/photo1.jpg', 2048, 1536,
     'COMPLETED', 'pred_abc123', 1, NULL, 'GOOD', '복원 품질 좋아요!', NOW(), NOW()),

    (5, 'restorations/5/original/photo2.jpg', 'restorations/5/mask/photo2.jpg',
     NULL, NULL, NULL,
     'PROCESSING', 'pred_def456', 1, NULL, NULL, NULL, NOW(), NOW());

-- =====================================================
-- 25. Post (게시글) - camelCase 컬럼명 주의!
-- =====================================================
INSERT INTO post (member_id, photo_lab_id, isSelfDeveloped, title, content, labReview, likeCount, commentCount, status, created_at, updated_at)
VALUES
    (1, 1, false, '강남 필름 첫 방문', '강남 필름 현상소에서 첫 현상했어요. 결과물이 너무 좋아요!', '친절하시고 실력도 좋으세요', 5, 3, 'ACTIVE', NOW(), NOW()),
    (2, 1, false, '흑백 필름 현상 후기', '흑백 필름 현상 정말 만족스럽습니다', '빠르고 깔끔해요', 8, 2, 'ACTIVE', NOW(), NOW()),
    (3, NULL, true, '집에서 자가 현상', '집에서 C-41 현상 도전해봤어요', NULL, 12, 5, 'ACTIVE', NOW(), NOW()),
    (5, 2, false, '마포 스튜디오 추천', '감성 충만한 현상소', '분위기 최고', 15, 7, 'ACTIVE', NOW(), NOW()),
    (1, 3, false, '해운대 필름 연구소', '부산 여행 가서 현상했어요', '실력 인정', 6, 1, 'ACTIVE', NOW(), NOW()),
    (2, NULL, true, '자가 현상 팁', '자가 현상 초보를 위한 팁 공유', NULL, 20, 10, 'ACTIVE', NOW(), NOW()),
    (4, 1, false, '처음 필름 현상', '인생 첫 필름 현상 성공!', '초보에게 친절하게 설명해주세요', 3, 0, 'ACTIVE', NOW(), NOW()),
    (5, 3, false, '해운대 재방문', '또 갈 거예요', '항상 만족', 4, 2, 'ACTIVE', NOW(), NOW());

-- =====================================================
-- 26. PostImage (게시글 이미지)
-- =====================================================
INSERT INTO post_image (post_id, object_path, display_order, width, height, created_at, updated_at)
VALUES
    (1, 'posts/1/img1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (1, 'posts/1/img2.jpg', 1, 1920, 1080, NOW(), NOW()),
    (2, 'posts/2/img1.jpg', 0, 2048, 1536, NOW(), NOW()),
    (3, 'posts/3/img1.jpg', 0, 1600, 1200, NOW(), NOW()),
    (3, 'posts/3/img2.jpg', 1, 1600, 1200, NOW(), NOW()),
    (4, 'posts/4/img1.jpg', 0, 2048, 1536, NOW(), NOW()),
    (5, 'posts/5/img1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (6, 'posts/6/img1.jpg', 0, 1920, 1080, NOW(), NOW()),
    (6, 'posts/6/img2.jpg', 1, 1920, 1080, NOW(), NOW()),
    (6, 'posts/6/img3.jpg', 2, 1920, 1080, NOW(), NOW());

-- =====================================================
-- 27. PostLike (좋아요)
-- =====================================================
INSERT INTO post_like (post_id, member_id, created_at, updated_at)
VALUES
    (1, 2, NOW(), NOW()), (1, 3, NOW(), NOW()), (1, 4, NOW(), NOW()), (1, 5, NOW(), NOW()), (1, 1, NOW(), NOW()),
    (2, 1, NOW(), NOW()), (2, 3, NOW(), NOW()), (2, 4, NOW(), NOW()), (2, 5, NOW(), NOW()), (2, 2, NOW(), NOW()), (2, 6, NOW(), NOW()), (2, 7, NOW(), NOW()), (2, 8, NOW(), NOW()),
    (3, 1, NOW(), NOW()), (3, 2, NOW(), NOW()), (3, 4, NOW(), NOW()), (3, 5, NOW(), NOW()), (3, 6, NOW(), NOW()), (3, 7, NOW(), NOW()), (3, 8, NOW(), NOW()), (3, 3, NOW(), NOW()), (3, 9, NOW(), NOW()), (3, 1, NOW(), NOW()), (3, 2, NOW(), NOW()), (3, 4, NOW(), NOW()),
    (4, 1, NOW(), NOW()), (4, 2, NOW(), NOW()), (4, 3, NOW(), NOW()), (4, 4, NOW(), NOW()), (4, 5, NOW(), NOW()), (4, 6, NOW(), NOW()), (4, 7, NOW(), NOW()), (4, 8, NOW(), NOW()), (4, 9, NOW(), NOW()), (4, 1, NOW(), NOW()), (4, 2, NOW(), NOW()), (4, 3, NOW(), NOW()), (4, 4, NOW(), NOW()), (4, 5, NOW(), NOW()), (4, 6, NOW(), NOW()),
    (5, 1, NOW(), NOW()), (5, 2, NOW(), NOW()), (5, 3, NOW(), NOW()), (5, 4, NOW(), NOW()), (5, 5, NOW(), NOW()), (5, 6, NOW(), NOW()),
    (6, 1, NOW(), NOW()), (6, 2, NOW(), NOW()), (6, 3, NOW(), NOW()), (6, 4, NOW(), NOW()), (6, 5, NOW(), NOW()), (6, 6, NOW(), NOW()), (6, 7, NOW(), NOW()), (6, 8, NOW(), NOW()), (6, 9, NOW(), NOW()), (6, 1, NOW(), NOW()), (6, 2, NOW(), NOW()), (6, 3, NOW(), NOW()), (6, 4, NOW(), NOW()), (6, 5, NOW(), NOW()), (6, 6, NOW(), NOW()), (6, 7, NOW(), NOW()), (6, 8, NOW(), NOW()), (6, 9, NOW(), NOW()), (6, 1, NOW(), NOW()), (6, 2, NOW(), NOW()),
    (7, 1, NOW(), NOW()), (7, 2, NOW(), NOW()), (7, 5, NOW(), NOW()),
    (8, 1, NOW(), NOW()), (8, 2, NOW(), NOW()), (8, 3, NOW(), NOW()), (8, 4, NOW(), NOW());

-- =====================================================
-- 28. Comment (댓글)
-- =====================================================
INSERT INTO comments (post_id, member_id, content, status, created_at, updated_at)
VALUES
    (1, 2, '저도 가보고 싶어요!', 'ACTIVE', NOW(), NOW()),
    (1, 3, '사진 잘 나왔네요', 'ACTIVE', NOW(), NOW()),
    (1, 4, '추천 감사합니다', 'ACTIVE', NOW(), NOW()),
    (2, 1, '흑백 필름 도전해보고 싶네요', 'ACTIVE', NOW(), NOW()),
    (2, 5, '다음에 저도 가볼게요', 'ACTIVE', NOW(), NOW()),
    (3, 1, '자가현상 어렵지 않나요?', 'ACTIVE', NOW(), NOW()),
    (3, 2, '집에서 하면 비용 절약되나요?', 'ACTIVE', NOW(), NOW()),
    (3, 4, '도전해보고 싶어요', 'ACTIVE', NOW(), NOW()),
    (3, 5, '팁 공유 부탁드려요', 'ACTIVE', NOW(), NOW()),
    (3, 2, '장비 추천 부탁드립니다', 'ACTIVE', NOW(), NOW()),
    (4, 1, '마포 스튜디오 좋죠!', 'ACTIVE', NOW(), NOW()),
    (4, 2, '저도 단골이에요', 'ACTIVE', NOW(), NOW()),
    (4, 3, '분위기 좋더라구요', 'ACTIVE', NOW(), NOW()),
    (4, 5, '재방문 의사 100%', 'ACTIVE', NOW(), NOW()),
    (4, 6, '사장님 친절하세요', 'ACTIVE', NOW(), NOW()),
    (4, 7, '가격도 합리적', 'ACTIVE', NOW(), NOW()),
    (4, 8, '추천합니다', 'ACTIVE', NOW(), NOW()),
    (5, 2, '부산 여행 가면 꼭 가봐야겠어요', 'ACTIVE', NOW(), NOW()),
    (6, 1, '팁 감사합니다!', 'ACTIVE', NOW(), NOW()),
    (6, 2, '도움됐어요', 'ACTIVE', NOW(), NOW());

-- =====================================================
-- 29. Inquiry (1:1 문의)
-- =====================================================
INSERT INTO inquiry (member_id, photo_lab_id, title, content, status, created_at, updated_at)
VALUES
    (1, 1, '예약 변경 문의', '예약 날짜 변경 가능한가요?', 'ANSWERED', NOW(), NOW()),
    (2, NULL, '서비스 문의', '회원탈퇴는 어떻게 하나요?', 'PENDING', NOW(), NOW()),
    (3, 2, '가격 문의', '대량 현상 할인 있나요?', 'ANSWERED', NOW(), NOW()),
    (4, 3, '배송 문의', '제주도 배송 가능한가요?', 'PENDING', NOW(), NOW());

-- =====================================================
-- 30. InquiryReply (문의 답변)
-- =====================================================
INSERT INTO inquiry_reply (inquiry_id, replier_id, content, created_at, updated_at)
VALUES
    (1, 6, '예약 변경은 2일 전까지 가능합니다. 마이페이지에서 변경해주세요.', NOW(), NOW()),
    (3, 7, '10롤 이상부터 10% 할인 적용됩니다.', NOW(), NOW());

-- =====================================================
-- 31. InquiryImage (문의 이미지)
-- =====================================================
INSERT INTO inquiry_image (inquiry_id, object_path, display_order, created_at, updated_at)
VALUES
    (1, 'inquiries/1/image1.jpg', 0, NOW(), NOW()),
    (3, 'inquiries/3/image1.jpg', 0, NOW(), NOW());

-- =====================================================
-- 32. Payment (결제 - 포트원 V2)
-- =====================================================
INSERT INTO payment (
    member_id, order_type, related_order_id, payment_id, order_name, amount, token_amount,
    transaction_id, pg_tx_id, pg_provider, method, status,
    card_company, card_number, approve_no, installment_months, receipt_url,
    requested_at, paid_at, created_at, updated_at
)
VALUES
    (2, 'TOKEN_PURCHASE', NULL, 'PAY_TOKEN_20260115_001', 'AI 복원 토큰 2개', 4000, 2,
     'txn_abc123', 'pg_kcp_123', 'KCP', 'CARD', 'PAID',
     '신한', '1234****5678', 'APPR123', 0, 'https://receipt.portone.io/abc123',
     '2026-01-15 14:30:00', '2026-01-15 14:30:05', NOW(), NOW()),

    (5, 'TOKEN_PURCHASE', NULL, 'PAY_TOKEN_20260116_001', 'AI 복원 토큰 7개', 14000, 7,
     'txn_def456', 'pg_kcp_456', 'KCP', 'EASY_PAY', 'PAID',
     '카카오페이', NULL, 'APPR456', 0, 'https://receipt.portone.io/def456',
     '2026-01-16 10:15:00', '2026-01-16 10:15:03', NOW(), NOW()),

    (2, 'PRINT_ORDER', 1, 'PAY_PRINT_20260116_001', '인화 주문', 25000, NULL,
     'txn_ghi789', 'pg_kcp_789', 'KCP', 'CARD', 'PAID',
     '국민', '5678****1234', 'APPR789', 0, 'https://receipt.portone.io/ghi789',
     '2026-01-16 16:20:00', '2026-01-16 16:20:07', NOW(), NOW());

-- =====================================================
-- 데이터 확인
-- =====================================================
SELECT 'Mock 데이터 INSERT 완료!' AS status;
SELECT 'member 수:', COUNT(*) FROM member;
SELECT 'photo_lab 수:', COUNT(*) FROM photo_lab;
SELECT 'reservation 수:', COUNT(*) FROM reservation;
SELECT 'post 수:', COUNT(*) FROM post;
