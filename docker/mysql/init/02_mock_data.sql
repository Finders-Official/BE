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
-- 생성일: 2026-01-15
-- ============================================

USE finders;

-- ============================================
-- 0. 기존 데이터 삭제 (FK 역순)
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE token_history;
TRUNCATE TABLE member_agreement;
TRUNCATE TABLE member_address;
TRUNCATE TABLE social_account;
TRUNCATE TABLE favorite_photo_lab;
TRUNCATE TABLE post_like;
TRUNCATE TABLE comments;
TRUNCATE TABLE post_image;
TRUNCATE TABLE post;
TRUNCATE TABLE photo_restoration;
TRUNCATE TABLE scanned_photo;
TRUNCATE TABLE print_order;
TRUNCATE TABLE development_order;
TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_slot;
TRUNCATE TABLE photo_lab_document;
TRUNCATE TABLE photo_lab_business_hour;
TRUNCATE TABLE photo_lab_notice;
TRUNCATE TABLE photo_lab_keyword;
TRUNCATE TABLE photo_lab_image;
TRUNCATE TABLE photo_lab;
TRUNCATE TABLE member_admin;
TRUNCATE TABLE member_owner;
TRUNCATE TABLE member_user;
TRUNCATE TABLE member;
TRUNCATE TABLE terms;
TRUNCATE TABLE region;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. REGION (지역)
-- ============================================
-- 시/도 (sido = NULL)
INSERT INTO region (id, sido, sigungu, created_at, updated_at) VALUES
(1, NULL, '서울특별시', NOW(), NOW()),
(2, NULL, '부산광역시', NOW(), NOW()),
(3, NULL, '경기도', NOW(), NOW()),
(4, NULL, '인천광역시', NOW(), NOW());

-- 시/군/구 (sido = 상위 region.id)
INSERT INTO region (id, sido, sigungu, created_at, updated_at) VALUES
(10, 1, '마포구', NOW(), NOW()),
(11, 1, '성동구', NOW(), NOW()),
(12, 1, '강남구', NOW(), NOW()),
(13, 1, '종로구', NOW(), NOW()),
(14, 1, '서초구', NOW(), NOW()),
(20, 2, '해운대구', NOW(), NOW()),
(21, 2, '수영구', NOW(), NOW()),
(30, 3, '성남시', NOW(), NOW()),
(31, 3, '수원시', NOW(), NOW()),
(40, 4, '연수구', NOW(), NOW());

-- ============================================
-- 2. TERMS (약관)
-- ============================================
INSERT INTO terms (id, type, version, title, content, is_required, is_active, effective_date, created_at, updated_at) VALUES
(1, 'SERVICE', '1.0', '서비스 이용약관', '파인더스 서비스 이용약관 내용입니다. 본 약관은 파인더스가 제공하는 필름 현상 예약 서비스 이용에 관한 기본적인 사항을 규정합니다.', true, true, '2025-01-01', NOW(), NOW()),
(2, 'PRIVACY', '1.0', '개인정보 처리방침', '파인더스 개인정보 처리방침입니다. 회원의 개인정보는 서비스 제공 목적으로만 사용되며, 관련 법령에 따라 안전하게 관리됩니다.', true, true, '2025-01-01', NOW(), NOW()),
(3, 'LOCATION', '1.0', '위치기반 서비스 이용약관', '위치기반 서비스 이용에 관한 약관입니다. 주변 현상소 검색을 위해 위치 정보를 활용합니다.', false, true, '2025-01-01', NOW(), NOW()),
(4, 'NOTIFICATION', '1.0', '마케팅 정보 수신 동의', '이벤트, 프로모션 등 마케팅 정보 수신에 동의합니다.', false, true, '2025-01-01', NOW(), NOW());

-- ============================================
-- 3. MEMBER (회원 - Base 테이블)
-- ============================================
-- USER 5명
INSERT INTO member (id, role, name, email, phone, status, refresh_token_hash, created_at, updated_at, deleted_at) VALUES
(1, 'USER', '김필름', 'film.kim@gmail.com', '010-1234-5678', 'ACTIVE', NULL, NOW(), NOW(), NULL),
(2, 'USER', '이현상', 'hyunsang.lee@naver.com', '010-2345-6789', 'ACTIVE', NULL, NOW(), NOW(), NULL),
(3, 'USER', '박스캔', 'scan.park@kakao.com', '010-3456-7890', 'ACTIVE', NULL, NOW(), NOW(), NULL),
(4, 'USER', '최인화', 'print.choi@gmail.com', '010-4567-8901', 'ACTIVE', NULL, NOW(), NOW(), NULL),
(5, 'USER', '정복원', 'restore.jung@naver.com', '010-5678-9012', 'ACTIVE', NULL, NOW(), NOW(), NULL);

-- OWNER 3명
INSERT INTO member (id, role, name, email, phone, status, refresh_token_hash, created_at, updated_at, deleted_at) VALUES
(101, 'OWNER', '홍사장', 'owner.hong@filmlab.com', '010-1111-1111', 'ACTIVE', NULL, NOW(), NOW(), NULL),
(102, 'OWNER', '김대표', 'owner.kim@photolab.com', '010-2222-2222', 'ACTIVE', NULL, NOW(), NOW(), NULL),
(103, 'OWNER', '박관장', 'owner.park@studio.com', '010-3333-3333', 'ACTIVE', NULL, NOW(), NOW(), NULL);

-- ADMIN 1명
INSERT INTO member (id, role, name, email, phone, status, refresh_token_hash, created_at, updated_at, deleted_at) VALUES
(201, 'ADMIN', '관리자', 'admin@finders.com', '010-9999-9999', 'ACTIVE', NULL, NOW(), NOW(), NULL);

-- ============================================
-- 4. MEMBER_USER (User 전용)
-- ============================================
-- token_balance는 token_history의 최종 balance_after와 일치
INSERT INTO member_user (member_id, nickname, profile_image, token_balance, last_token_refresh_at) VALUES
(1, '필름매니아', 'profiles/1/default.jpg', 9, '2026-01-15 00:00:00'),
(2, '현상러버', 'profiles/2/default.jpg', 3, NULL),
(3, '스캔마스터', 'profiles/3/default.jpg', 12, '2026-01-10 10:00:00'),
(4, '인화장인', 'profiles/4/default.jpg', 2, NULL),
(5, '복원전문가', 'profiles/5/default.jpg', 2, '2026-01-14 15:30:00');

-- ============================================
-- 5. MEMBER_OWNER (Owner 전용)
-- ============================================
INSERT INTO member_owner (member_id, password_hash, business_number, bank_name, bank_account_number, bank_account_holder) VALUES
(101, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.c3ptvmVQyJtUZSrx2u', '123-45-67890', '신한은행', '110-123-456789', '홍길동'),
(102, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.c3ptvmVQyJtUZSrx2u', '234-56-78901', '국민은행', '123-456-789012', '김철수'),
(103, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.c3ptvmVQyJtUZSrx2u', '345-67-89012', '우리은행', '1002-123-456789', '박영희');

-- ============================================
-- 6. MEMBER_ADMIN (Admin 전용)
-- ============================================
INSERT INTO member_admin (member_id, password_hash) VALUES
(201, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.c3ptvmVQyJtUZSrx2u');

-- ============================================
-- 7. SOCIAL_ACCOUNT (소셜 로그인)
-- ============================================
INSERT INTO social_account (id, member_id, provider, provider_id, social_email, created_at, updated_at, deleted_at) VALUES
(1, 1, 'KAKAO', 'kakao_123456789', 'film.kim@gmail.com', NOW(), NOW(), NULL),
(2, 2, 'KAKAO', 'kakao_234567890', 'hyunsang.lee@naver.com', NOW(), NOW(), NULL),
(3, 3, 'APPLE', 'apple_345678901', 'scan.park@kakao.com', NOW(), NOW(), NULL),
(4, 4, 'KAKAO', 'kakao_456789012', 'print.choi@gmail.com', NOW(), NOW(), NULL),
(5, 5, 'APPLE', 'apple_567890123', 'restore.jung@naver.com', NOW(), NOW(), NULL);

-- ============================================
-- 8. MEMBER_ADDRESS (배송지)
-- ============================================
INSERT INTO member_address (id, member_id, address_name, recipient_name, phone, zipcode, address, address_detail, is_default, created_at, updated_at, deleted_at) VALUES
(1, 1, '집', '김필름', '010-1234-5678', '04083', '서울 마포구 월드컵북로 396', '누리꿈스퀘어 연구개발타워 15층', true, NOW(), NOW(), NULL),
(2, 1, '회사', '김필름', '010-1234-5678', '06164', '서울 강남구 테헤란로 521', '파르나스타워 30층', false, NOW(), NOW(), NULL),
(3, 2, '우리집', '이현상', '010-2345-6789', '04778', '서울 성동구 왕십리로 83-21', '한양대 프라자 3층', true, NOW(), NOW(), NULL),
(4, 3, '본가', '박스캔', '010-3456-7890', '48060', '부산 해운대구 해운대해변로 30', '해운대자이 102동 1503호', true, NOW(), NOW(), NULL);

-- ============================================
-- 9. MEMBER_AGREEMENT (약관 동의)
-- ============================================
INSERT INTO member_agreement (id, member_id, terms_id, is_agreed, agreed_at, created_at, updated_at) VALUES
(1, 1, 1, true, NOW(), NOW(), NOW()),
(2, 1, 2, true, NOW(), NOW(), NOW()),
(3, 1, 3, true, NOW(), NOW(), NOW()),
(4, 1, 4, true, NOW(), NOW(), NOW()),
(5, 2, 1, true, NOW(), NOW(), NOW()),
(6, 2, 2, true, NOW(), NOW(), NOW()),
(7, 2, 3, false, NOW(), NOW(), NOW()),
(8, 3, 1, true, NOW(), NOW(), NOW()),
(9, 3, 2, true, NOW(), NOW(), NOW()),
(10, 4, 1, true, NOW(), NOW(), NOW()),
(11, 4, 2, true, NOW(), NOW(), NOW()),
(12, 5, 1, true, NOW(), NOW(), NOW()),
(13, 5, 2, true, NOW(), NOW(), NOW());

-- ============================================
-- 10. PHOTO_LAB (현상소)
-- ============================================
INSERT INTO photo_lab (id, owner_id, region_id, name, description, phone, zipcode, address, address_detail, latitude, longitude, work_count, post_count, reservation_count, avg_work_time, status, is_delivery_available, max_reservations_per_hour, qr_code_url, created_at, updated_at, deleted_at) VALUES
(1, 101, 10, '홍대필름랩', '홍대 감성 가득한 필름 현상소입니다. 따뜻한 색감과 빈티지한 톤 보정이 특징이에요. 10년 경력의 현상 장인이 직접 작업합니다.', '02-123-4567', '04003', '서울 마포구 와우산로 94', '2층', 37.5563, 126.9235, 1247, 89, 456, 72, 'ACTIVE', true, 5, 'photo-labs/1/qr.png', NOW(), NOW(), NULL),
(2, 102, 11, '성수필름스튜디오', '성수동 핫플! MZ세대가 사랑하는 청량한 색감 전문 현상소. 당일 스캔 가능, 프리미엄 스캔 서비스 제공.', '02-234-5678', '04780', '서울 성동구 성수이로 88', '성수 SK V1 타워 B1', 37.5447, 127.0556, 2156, 156, 823, 48, 'ACTIVE', true, 8, 'photo-labs/2/qr.png', NOW(), NOW(), NULL),
(3, 103, 20, '해운대필름하우스', '부산 해운대 바다가 보이는 현상소. 여행 필름 전문, 영화용 필름 현상 가능. 택배 접수 환영!', '051-345-6789', '48094', '부산 해운대구 달맞이길 30', '1층', 35.1587, 129.1604, 567, 45, 234, 96, 'ACTIVE', true, 3, 'photo-labs/3/qr.png', NOW(), NOW(), NULL);

-- ============================================
-- 11. PHOTO_LAB_IMAGE (현상소 이미지)
-- ============================================
INSERT INTO photo_lab_image (id, photo_lab_id, image_url, display_order, is_main, created_at, updated_at) VALUES
(1, 1, 'photo-labs/1/images/main.jpg', 0, true, NOW(), NOW()),
(2, 1, 'photo-labs/1/images/interior1.jpg', 1, false, NOW(), NOW()),
(3, 1, 'photo-labs/1/images/interior2.jpg', 2, false, NOW(), NOW()),
(4, 1, 'photo-labs/1/images/sample1.jpg', 3, false, NOW(), NOW()),
(5, 2, 'photo-labs/2/images/main.jpg', 0, true, NOW(), NOW()),
(6, 2, 'photo-labs/2/images/interior1.jpg', 1, false, NOW(), NOW()),
(7, 2, 'photo-labs/2/images/sample1.jpg', 2, false, NOW(), NOW()),
(8, 3, 'photo-labs/3/images/main.jpg', 0, true, NOW(), NOW()),
(9, 3, 'photo-labs/3/images/ocean.jpg', 1, false, NOW(), NOW());

-- ============================================
-- 12. PHOTO_LAB_KEYWORD (현상소 키워드)
-- ============================================
INSERT INTO photo_lab_keyword (id, photo_lab_id, keyword, created_at, updated_at) VALUES
(1, 1, '따뜻한 색감', NOW(), NOW()),
(2, 1, '빈티지한', NOW(), NOW()),
(3, 1, '택배 접수', NOW(), NOW()),
(4, 2, '청량한', NOW(), NOW()),
(5, 2, '당일 스캔', NOW(), NOW()),
(6, 2, 'MZ 감성', NOW(), NOW()),
(7, 3, '영화용 필름', NOW(), NOW()),
(8, 3, '바다 뷰', NOW(), NOW()),
(9, 3, '택배 접수', NOW(), NOW());

-- ============================================
-- 13. PHOTO_LAB_NOTICE (현상소 공지)
-- ============================================
INSERT INTO photo_lab_notice (id, photo_lab_id, title, content, notice_type, start_date, end_date, is_active, created_at, updated_at) VALUES
(1, 1, '설 연휴 휴무 안내', '2026년 1월 28일 ~ 2월 2일까지 설 연휴로 휴무입니다. 연휴 전 접수건은 2월 3일부터 순차 작업됩니다.', 'GENERAL', '2026-01-20', '2026-02-03', true, NOW(), NOW()),
(2, 1, '신규 회원 10% 할인 이벤트', '1월 한달간 첫 현상 주문 시 10% 할인해 드립니다!', 'EVENT', '2026-01-01', '2026-01-31', true, NOW(), NOW()),
(3, 2, '프리미엄 스캔 서비스 오픈', '고해상도 프리미엄 스캔 서비스를 시작합니다. 6000dpi 스캔으로 더 선명한 사진을 만나보세요.', 'GENERAL', NULL, NULL, true, NOW(), NOW()),
(4, 3, '택배 접수 배송비 무료 이벤트', '2월까지 택배 접수 시 왕복 배송비 무료!', 'EVENT', '2026-01-15', '2026-02-28', true, NOW(), NOW());

-- ============================================
-- 14. PHOTO_LAB_BUSINESS_HOUR (영업시간)
-- ============================================
-- 홍대필름랩 (월-토 10:00-20:00, 일 휴무)
INSERT INTO photo_lab_business_hour (id, photo_lab_id, day_of_week, open_time, close_time, is_closed, created_at, updated_at) VALUES
(1, 1, 'MONDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
(2, 1, 'TUESDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
(3, 1, 'WEDNESDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
(4, 1, 'THURSDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
(5, 1, 'FRIDAY', '10:00:00', '20:00:00', false, NOW(), NOW()),
(6, 1, 'SATURDAY', '11:00:00', '18:00:00', false, NOW(), NOW()),
(7, 1, 'SUNDAY', NULL, NULL, true, NOW(), NOW());

-- 성수필름스튜디오 (월-금 09:00-21:00, 토 10:00-18:00, 일 휴무)
INSERT INTO photo_lab_business_hour (id, photo_lab_id, day_of_week, open_time, close_time, is_closed, created_at, updated_at) VALUES
(8, 2, 'MONDAY', '09:00:00', '21:00:00', false, NOW(), NOW()),
(9, 2, 'TUESDAY', '09:00:00', '21:00:00', false, NOW(), NOW()),
(10, 2, 'WEDNESDAY', '09:00:00', '21:00:00', false, NOW(), NOW()),
(11, 2, 'THURSDAY', '09:00:00', '21:00:00', false, NOW(), NOW()),
(12, 2, 'FRIDAY', '09:00:00', '21:00:00', false, NOW(), NOW()),
(13, 2, 'SATURDAY', '10:00:00', '18:00:00', false, NOW(), NOW()),
(14, 2, 'SUNDAY', NULL, NULL, true, NOW(), NOW());

-- 해운대필름하우스 (화-일 11:00-19:00, 월 휴무)
INSERT INTO photo_lab_business_hour (id, photo_lab_id, day_of_week, open_time, close_time, is_closed, created_at, updated_at) VALUES
(15, 3, 'MONDAY', NULL, NULL, true, NOW(), NOW()),
(16, 3, 'TUESDAY', '11:00:00', '19:00:00', false, NOW(), NOW()),
(17, 3, 'WEDNESDAY', '11:00:00', '19:00:00', false, NOW(), NOW()),
(18, 3, 'THURSDAY', '11:00:00', '19:00:00', false, NOW(), NOW()),
(19, 3, 'FRIDAY', '11:00:00', '19:00:00', false, NOW(), NOW()),
(20, 3, 'SATURDAY', '11:00:00', '19:00:00', false, NOW(), NOW()),
(21, 3, 'SUNDAY', '12:00:00', '18:00:00', false, NOW(), NOW());

-- ============================================
-- 15. PHOTO_LAB_DOCUMENT (사업자 서류)
-- ============================================
INSERT INTO photo_lab_document (id, photo_lab_id, document_type, file_url, file_name, verified_at, created_at, updated_at) VALUES
(1, 1, 'BUSINESS_LICENSE', 'photo-labs/1/documents/business_license.pdf', '사업자등록증_홍대필름랩.pdf', '2025-12-01 10:00:00', NOW(), NOW()),
(2, 2, 'BUSINESS_LICENSE', 'photo-labs/2/documents/business_license.pdf', '사업자등록증_성수필름스튜디오.pdf', '2025-12-05 14:30:00', NOW(), NOW()),
(3, 3, 'BUSINESS_LICENSE', 'photo-labs/3/documents/business_license.pdf', '사업자등록증_해운대필름하우스.pdf', '2025-12-10 09:00:00', NOW(), NOW());

-- ============================================
-- 16. RESERVATION_SLOT (예약 슬롯)
-- ============================================
-- reserved_count는 실제 reservation 레코드 수와 일치
INSERT INTO reservation_slot (id, photo_lab_id, reservation_date, reservation_time, max_capacity, reserved_count, created_at, updated_at) VALUES
-- 홍대필름랩 1/15
(1, 1, '2026-01-15', '10:00:00', 5, 1, NOW(), NOW()),
(2, 1, '2026-01-15', '11:00:00', 5, 1, NOW(), NOW()),
(3, 1, '2026-01-15', '14:00:00', 5, 1, NOW(), NOW()),
(4, 1, '2026-01-15', '15:00:00', 5, 0, NOW(), NOW()),
-- 홍대필름랩 1/16
(5, 1, '2026-01-16', '10:00:00', 5, 1, NOW(), NOW()),
(6, 1, '2026-01-16', '14:00:00', 5, 0, NOW(), NOW()),
-- 성수필름스튜디오 1/15
(7, 2, '2026-01-15', '09:00:00', 8, 1, NOW(), NOW()),
(8, 2, '2026-01-15', '10:00:00', 8, 1, NOW(), NOW()),
(9, 2, '2026-01-15', '14:00:00', 8, 1, NOW(), NOW()),
-- 성수필름스튜디오 1/16
(10, 2, '2026-01-16', '10:00:00', 8, 0, NOW(), NOW()),
(11, 2, '2026-01-16', '15:00:00', 8, 0, NOW(), NOW()),
-- 해운대필름하우스 1/15
(12, 3, '2026-01-15', '11:00:00', 3, 1, NOW(), NOW()),
(13, 3, '2026-01-15', '14:00:00', 3, 1, NOW(), NOW());

-- ============================================
-- 17. RESERVATION (예약)
-- ============================================
INSERT INTO reservation (id, member_id, slot_id, photo_lab_id, status, is_develop, is_scan, is_print, roll_count, request_message, created_at, updated_at, deleted_at) VALUES
(1, 1, 1, 1, 'RESERVED', true, true, false, 2, '컬러 네거티브 2롤입니다. 따뜻한 톤으로 부탁드려요!', NOW(), NOW(), NULL),
(2, 1, 7, 2, 'RESERVED', true, true, true, 3, '흑백 필름 3롤, 4x6 인화 각 1장씩 부탁드립니다.', NOW(), NOW(), NULL),
(3, 2, 2, 1, 'RESERVED', true, true, false, 1, '코닥 포트라 400 1롤', NOW(), NOW(), NULL),
(4, 2, 8, 2, 'COMPLETED', true, true, false, 2, '후지 스페리아 2롤 현상 스캔 요청', '2026-01-10 10:00:00', '2026-01-12 15:00:00', NULL),
(5, 3, 12, 3, 'RESERVED', true, true, false, 4, '여행 사진 4롤! 바다 색감 살려주세요~', NOW(), NOW(), NULL),
(6, 3, 9, 2, 'COMPLETED', true, true, true, 1, '일포드 HP5 1롤, 8x10 인화 5장', '2026-01-08 14:00:00', '2026-01-11 10:00:00', NULL),
(7, 4, 3, 1, 'RESERVED', false, true, false, 2, '이미 현상된 필름 스캔만 요청합니다.', NOW(), NOW(), NULL),
(8, 4, 10, 2, 'CANCELED', true, true, false, 1, '취소합니다.', '2026-01-13 09:00:00', '2026-01-14 10:00:00', '2026-01-14 10:00:00'),
(9, 5, 5, 1, 'RESERVED', true, true, true, 2, '코닥 에크타 100 2롤, 5x7 인화 각 2장', NOW(), NOW(), NULL),
(10, 5, 13, 3, 'RESERVED', true, true, false, 3, '시네스틸 800T 3롤, 영화 느낌으로!', NOW(), NOW(), NULL);

-- ============================================
-- 18. DEVELOPMENT_ORDER (현상 주문)
-- ============================================
INSERT INTO development_order (id, reservation_id, photo_lab_id, member_id, order_code, status, is_develop, is_scan, is_print, roll_count, total_photos, total_price, completed_at, created_at, updated_at) VALUES
(1, 4, 2, 2, 'DEV-20260110-001', 'COMPLETED', true, true, false, 2, 72, 28000, '2026-01-12 15:00:00', '2026-01-10 10:30:00', '2026-01-12 15:00:00'),
(2, 6, 2, 3, 'DEV-20260108-001', 'COMPLETED', true, true, true, 1, 36, 45000, '2026-01-11 10:00:00', '2026-01-08 14:30:00', '2026-01-11 10:00:00'),
(3, NULL, 1, 1, 'DEV-20260114-001', 'SCANNING', true, true, false, 1, 36, 15000, NULL, '2026-01-14 11:00:00', NOW()),
(4, NULL, 2, 4, 'DEV-20260113-001', 'DEVELOPING', true, true, false, 2, 0, 24000, NULL, '2026-01-13 16:00:00', NOW()),
(5, NULL, 3, 5, 'DEV-20260112-001', 'RECEIVED', true, true, true, 3, 0, 55000, NULL, '2026-01-12 13:00:00', NOW());

-- ============================================
-- 19. SCANNED_PHOTO (스캔 사진)
-- ============================================
INSERT INTO scanned_photo (id, order_id, image_key, file_name, display_order, created_at, updated_at) VALUES
-- 주문1 (72장)
(1, 1, 'temp/orders/1/scans/001.jpg', 'IMG_001.jpg', 1, NOW(), NOW()),
(2, 1, 'temp/orders/1/scans/002.jpg', 'IMG_002.jpg', 2, NOW(), NOW()),
(3, 1, 'temp/orders/1/scans/003.jpg', 'IMG_003.jpg', 3, NOW(), NOW()),
(4, 1, 'temp/orders/1/scans/004.jpg', 'IMG_004.jpg', 4, NOW(), NOW()),
(5, 1, 'temp/orders/1/scans/005.jpg', 'IMG_005.jpg', 5, NOW(), NOW()),
-- 주문2 (36장)
(6, 2, 'temp/orders/2/scans/001.jpg', 'BW_001.jpg', 1, NOW(), NOW()),
(7, 2, 'temp/orders/2/scans/002.jpg', 'BW_002.jpg', 2, NOW(), NOW()),
(8, 2, 'temp/orders/2/scans/003.jpg', 'BW_003.jpg', 3, NOW(), NOW()),
-- 주문3 (진행중 - 일부만 스캔됨)
(9, 3, 'temp/orders/3/scans/001.jpg', 'ROLL1_001.jpg', 1, NOW(), NOW()),
(10, 3, 'temp/orders/3/scans/002.jpg', 'ROLL1_002.jpg', 2, NOW(), NOW());

-- ============================================
-- 20. PRINT_ORDER (인화 주문)
-- ============================================
INSERT INTO print_order (id, dev_order_id, photo_lab_id, order_code, status, total_price, receipt_method, estimated_at, completed_at, created_at, updated_at) VALUES
(1, 2, 2, 'PRT-20260108-001', 'COMPLETED', 25000, 'PICKUP', '2026-01-11 10:00:00', '2026-01-11 10:00:00', '2026-01-08 15:00:00', '2026-01-11 10:00:00'),
(2, NULL, 1, 'PRT-20260114-001', 'PENDING', 12000, 'DELIVERY', '2026-01-17 18:00:00', NULL, NOW(), NOW());

-- ============================================
-- 21. PHOTO_RESTORATION (AI 사진 복원)
-- ============================================
INSERT INTO photo_restoration (id, member_id, original_url, mask_url, restored_url, status, replicate_prediction_id, token_used, error_message, feedback_rating, feedback_comment, created_at, updated_at) VALUES
(1, 1, 'restorations/1/original/photo1.jpg', 'restorations/1/mask/photo1_mask.png', 'restorations/1/restored/photo1_restored.jpg', 'COMPLETED', 'pred_abc123', 1, NULL, 'GOOD', '와 진짜 깔끔하게 복원됐어요!', '2026-01-10 14:00:00', '2026-01-10 14:05:00'),
(2, 1, 'restorations/1/original/photo2.jpg', 'restorations/1/mask/photo2_mask.png', 'restorations/1/restored/photo2_restored.jpg', 'COMPLETED', 'pred_def456', 1, NULL, NULL, NULL, '2026-01-12 11:00:00', '2026-01-12 11:03:00'),
(3, 3, 'restorations/3/original/old_photo.jpg', 'restorations/3/mask/old_photo_mask.png', 'restorations/3/restored/old_photo_restored.jpg', 'COMPLETED', 'pred_ghi789', 1, NULL, 'GOOD', '오래된 사진이 새것처럼!', '2026-01-13 16:30:00', '2026-01-13 16:35:00'),
(4, 5, 'restorations/5/original/scratch.jpg', 'restorations/5/mask/scratch_mask.png', NULL, 'PROCESSING', 'pred_jkl012', 1, NULL, NULL, NULL, NOW(), NOW()),
(5, 5, 'restorations/5/original/torn.jpg', 'restorations/5/mask/torn_mask.png', NULL, 'FAILED', 'pred_mno345', 1, '이미지 처리 중 오류가 발생했습니다.', NULL, NULL, '2026-01-14 10:00:00', '2026-01-14 10:02:00');

-- ============================================
-- 22. TOKEN_HISTORY (토큰 이력)
-- ============================================
INSERT INTO token_history (id, member_id, type, amount, balance_after, related_type, related_id, description, created_at, updated_at, deleted_at) VALUES
(1, 1, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스 토큰 지급', '2025-12-01 10:00:00', '2025-12-01 10:00:00', NULL),
(2, 1, 'PURCHASE', 5, 8, 'PAYMENT', 1, '토큰 5개 구매', '2026-01-05 14:00:00', '2026-01-05 14:00:00', NULL),
(3, 1, 'USE', -1, 7, 'PHOTO_RESTORATION', 1, 'AI 복원 사용', '2026-01-10 14:00:00', '2026-01-10 14:00:00', NULL),
(4, 1, 'USE', -1, 6, 'PHOTO_RESTORATION', 2, 'AI 복원 사용', '2026-01-12 11:00:00', '2026-01-12 11:00:00', NULL),
(5, 1, 'REFRESH', 3, 9, NULL, NULL, '무료 토큰 리프레시', '2026-01-15 00:00:00', '2026-01-15 00:00:00', NULL),
(6, 2, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스 토큰 지급', '2025-12-10 09:00:00', '2025-12-10 09:00:00', NULL),
(7, 3, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스 토큰 지급', '2025-12-15 11:00:00', '2025-12-15 11:00:00', NULL),
(8, 3, 'PURCHASE', 10, 13, 'PAYMENT', 2, '토큰 10개 구매', '2026-01-10 10:00:00', '2026-01-10 10:00:00', NULL),
(9, 3, 'USE', -1, 12, 'PHOTO_RESTORATION', 3, 'AI 복원 사용', '2026-01-13 16:30:00', '2026-01-13 16:30:00', NULL),
(10, 4, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스 토큰 지급', '2025-12-20 15:00:00', '2025-12-20 15:00:00', NULL),
(11, 4, 'USE', -1, 2, 'PHOTO_RESTORATION', NULL, 'AI 복원 사용 (테스트)', '2026-01-14 09:00:00', '2026-01-14 09:00:00', NULL),
(12, 5, 'SIGNUP_BONUS', 3, 3, NULL, NULL, '회원가입 보너스 토큰 지급', '2025-12-25 18:00:00', '2025-12-25 18:00:00', NULL),
(13, 5, 'USE', -1, 2, 'PHOTO_RESTORATION', 4, 'AI 복원 사용', NOW(), NOW(), NULL),
(14, 5, 'USE', -1, 1, 'PHOTO_RESTORATION', 5, 'AI 복원 사용 (실패로 환불 예정)', '2026-01-14 10:00:00', '2026-01-14 10:00:00', NULL),
(15, 5, 'REFUND', 1, 2, 'PHOTO_RESTORATION', 5, 'AI 복원 실패 환불', '2026-01-14 10:05:00', '2026-01-14 10:05:00', NULL);

-- ============================================
-- 23. POST (게시글)
-- ============================================
-- like_count, comment_count는 실제 post_like, comments 레코드 수와 일치
INSERT INTO post (id, member_user_id, photo_lab_id, is_self_developed, title, content, lab_review, like_count, comment_count, status, created_at, updated_at, deleted_at) VALUES
(1, 1, 1, false, '홍대필름랩 첫 방문 후기', '오늘 처음으로 필름 현상을 맡겨봤어요. 코닥 포트라 400으로 찍은 사진들인데, 색감이 정말 따뜻하고 예쁘게 나왔어요! 사장님도 친절하시고 설명도 자세하게 해주셨습니다. 다음에도 여기서 현상할 예정이에요 ㅎㅎ', '따뜻한 색감이 매력적인 곳!', 4, 3, 'ACTIVE', '2026-01-11 18:00:00', '2026-01-11 18:00:00', NULL),
(2, 2, 2, false, '성수 핫플 필름 현상소 추천', '요즘 성수에서 제일 핫한 필름 현상소라고 해서 가봤는데 진짜 대박... 청량한 색감이 미쳤어요. 당일 스캔도 가능해서 급할 때 딱이에요!', 'MZ 감성 청량한 색감 최고', 4, 5, 'ACTIVE', '2026-01-12 14:30:00', '2026-01-12 14:30:00', NULL),
(3, 3, NULL, true, '집에서 흑백 필름 자가현상 도전기', '드디어 자가현상에 도전했습니다! 일포드 HP5를 D-76 현상액으로 현상했는데, 생각보다 어렵지 않았어요. 암실 작업의 매력에 빠졌습니다.', NULL, 4, 4, 'ACTIVE', '2026-01-09 21:00:00', '2026-01-09 21:00:00', NULL),
(4, 4, 3, false, '부산 해운대 필름하우스 다녀왔어요', '부산 여행 중에 들렀는데 바다가 보이는 현상소라니... 분위기가 너무 좋았어요. 시네스틸로 찍은 야경 사진 색감이 영화 같이 나왔습니다.', '바다 뷰 + 영화 색감 = 최고', 3, 3, 'ACTIVE', '2026-01-13 16:00:00', '2026-01-13 16:00:00', NULL),
(5, 5, 1, false, '코닥 에크타 100 현상 결과물 공유', '날씨 좋은 날 찍은 코닥 에크타 100 결과물이에요. 홍대필름랩에서 현상했는데 채도가 정말 선명하게 나왔네요.', '에크타 색감 잘 살려주셔요', 2, 2, 'ACTIVE', '2026-01-14 11:00:00', '2026-01-14 11:00:00', NULL),
(6, 1, 2, false, '성수 스튜디오 재방문 후기', '지난번에 너무 좋아서 또 왔어요. 이번엔 흑백 필름인데 톤이 정말 예술입니다.', '흑백도 잘하시네요!', 1, 1, 'ACTIVE', NOW(), NOW(), NULL),
(7, 2, NULL, true, '크로스 프로세싱 실험', '컬러 네거티브를 E-6 현상해봤어요. 독특한 색감이 나와서 신기하네요!', NULL, 2, 1, 'ACTIVE', '2026-01-08 20:00:00', '2026-01-08 20:00:00', NULL),
(8, 3, 1, false, '필름 입문자의 첫 현상 후기', '처음 필름 카메라를 사고 첫 현상을 홍대필름랩에서 했어요. 완전 떨렸는데 결과물 보고 감동... 필름의 매력에 빠졌습니다!', '초보자한테 친절해요', 2, 1, 'ACTIVE', '2026-01-07 19:30:00', '2026-01-07 19:30:00', NULL);

-- ============================================
-- 24. POST_IMAGE (게시글 이미지)
-- ============================================
INSERT INTO post_image (id, post_id, image_url, width, height, display_order, created_at, updated_at) VALUES
(1, 1, 'posts/1/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(2, 1, 'posts/1/img2.jpg', 1200, 800, 1, NOW(), NOW()),
(3, 1, 'posts/1/img3.jpg', 800, 1200, 2, NOW(), NOW()),
(4, 2, 'posts/2/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(5, 2, 'posts/2/img2.jpg', 1200, 800, 1, NOW(), NOW()),
(6, 3, 'posts/3/img1.jpg', 1200, 1200, 0, NOW(), NOW()),
(7, 3, 'posts/3/img2.jpg', 1200, 1200, 1, NOW(), NOW()),
(8, 3, 'posts/3/img3.jpg', 1200, 1200, 2, NOW(), NOW()),
(9, 3, 'posts/3/img4.jpg', 1200, 1200, 3, NOW(), NOW()),
(10, 4, 'posts/4/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(11, 4, 'posts/4/img2.jpg', 800, 1200, 1, NOW(), NOW()),
(12, 5, 'posts/5/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(13, 6, 'posts/6/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(14, 7, 'posts/7/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(15, 7, 'posts/7/img2.jpg', 1200, 800, 1, NOW(), NOW()),
(16, 8, 'posts/8/img1.jpg', 1200, 800, 0, NOW(), NOW()),
(17, 8, 'posts/8/img2.jpg', 1200, 800, 1, NOW(), NOW());

-- ============================================
-- 25. COMMENTS (댓글)
-- ============================================
INSERT INTO comments (id, post_id, member_user_id, content, status, created_at, updated_at, deleted_at) VALUES
(1, 1, 2, '저도 여기 가봤는데 진짜 좋았어요! 색감 대박', 'ACTIVE', '2026-01-11 19:00:00', '2026-01-11 19:00:00', NULL),
(2, 1, 3, '사진 너무 예뻐요 ㅠㅠ 저도 필름 시작하고 싶어지네요', 'ACTIVE', '2026-01-11 20:30:00', '2026-01-11 20:30:00', NULL),
(3, 1, 4, '포트라 400 색감 찐이네요', 'ACTIVE', '2026-01-12 09:00:00', '2026-01-12 09:00:00', NULL),
(4, 2, 1, '오 여기 저도 가봐야겠어요! 당일 스캔 가능한 거 좋네요', 'ACTIVE', '2026-01-12 15:00:00', '2026-01-12 15:00:00', NULL),
(5, 2, 3, '성수 요즘 핫하던데 여기구나', 'ACTIVE', '2026-01-12 16:00:00', '2026-01-12 16:00:00', NULL),
(6, 2, 4, '청량한 색감 좋아요!', 'ACTIVE', '2026-01-12 17:30:00', '2026-01-12 17:30:00', NULL),
(7, 2, 5, '다음에 가볼게요~', 'ACTIVE', '2026-01-12 18:00:00', '2026-01-12 18:00:00', NULL),
(8, 2, 1, '저도 다시 가고 싶어요', 'ACTIVE', '2026-01-13 10:00:00', '2026-01-13 10:00:00', NULL),
(9, 3, 1, '자가현상 멋있어요! 저도 도전해보고 싶네요', 'ACTIVE', '2026-01-10 09:00:00', '2026-01-10 09:00:00', NULL),
(10, 3, 2, '암실 작업 로망이에요...', 'ACTIVE', '2026-01-10 10:30:00', '2026-01-10 10:30:00', NULL),
(11, 3, 4, 'D-76이 입문용으로 좋다고 하더라고요', 'ACTIVE', '2026-01-10 11:00:00', '2026-01-10 11:00:00', NULL),
(12, 3, 5, '결과물 대박... 저도 해볼래요', 'ACTIVE', '2026-01-10 14:00:00', '2026-01-10 14:00:00', NULL),
(13, 4, 1, '바다가 보이는 현상소라니 낭만있다', 'ACTIVE', '2026-01-13 17:00:00', '2026-01-13 17:00:00', NULL),
(14, 4, 2, '시네스틸 야경 궁금해요!', 'ACTIVE', '2026-01-13 18:00:00', '2026-01-13 18:00:00', NULL),
(15, 4, 3, '부산 가면 꼭 가볼게요', 'ACTIVE', '2026-01-14 10:00:00', '2026-01-14 10:00:00', NULL),
(16, 5, 2, '에크타 색감 진짜 좋죠', 'ACTIVE', '2026-01-14 12:00:00', '2026-01-14 12:00:00', NULL),
(17, 5, 4, '채도가 정말 선명하네요!', 'ACTIVE', '2026-01-14 13:00:00', '2026-01-14 13:00:00', NULL),
(18, 6, 3, '흑백도 잘하시는군요', 'ACTIVE', NOW(), NOW(), NULL),
(19, 7, 1, '크로스 프로세싱 독특하네요!', 'ACTIVE', '2026-01-09 08:00:00', '2026-01-09 08:00:00', NULL),
(20, 8, 5, '필름 입문 축하드려요! 저도 처음엔 떨렸는데 ㅎㅎ', 'ACTIVE', '2026-01-08 09:00:00', '2026-01-08 09:00:00', NULL);

-- ============================================
-- 26. POST_LIKE (좋아요)
-- ============================================
INSERT INTO post_like (id, post_id, member_user_id, created_at, updated_at) VALUES
(1, 1, 2, NOW(), NOW()),
(2, 1, 3, NOW(), NOW()),
(3, 1, 4, NOW(), NOW()),
(4, 1, 5, NOW(), NOW()),
(5, 2, 1, NOW(), NOW()),
(6, 2, 3, NOW(), NOW()),
(7, 2, 4, NOW(), NOW()),
(8, 2, 5, NOW(), NOW()),
(9, 3, 1, NOW(), NOW()),
(10, 3, 2, NOW(), NOW()),
(11, 3, 4, NOW(), NOW()),
(12, 3, 5, NOW(), NOW()),
(13, 4, 1, NOW(), NOW()),
(14, 4, 2, NOW(), NOW()),
(15, 4, 3, NOW(), NOW()),
(16, 5, 2, NOW(), NOW()),
(17, 5, 3, NOW(), NOW()),
(18, 6, 2, NOW(), NOW()),
(19, 7, 1, NOW(), NOW()),
(20, 7, 4, NOW(), NOW()),
(21, 8, 2, NOW(), NOW()),
(22, 8, 5, NOW(), NOW());

-- ============================================
-- 27. FAVORITE_PHOTO_LAB (관심 현상소)
-- ============================================
INSERT INTO favorite_photo_lab (id, member_id, photo_lab_id, created_at, updated_at) VALUES
(1, 1, 1, NOW(), NOW()),
(2, 1, 2, NOW(), NOW()),
(3, 2, 2, NOW(), NOW()),
(4, 2, 3, NOW(), NOW()),
(5, 3, 1, NOW(), NOW()),
(6, 3, 3, NOW(), NOW()),
(7, 4, 3, NOW(), NOW()),
(8, 5, 1, NOW(), NOW()),
(9, 5, 2, NOW(), NOW());

-- ============================================
-- 완료!
-- ============================================
SELECT 'Mock data inserted successfully!' AS result;
SELECT
    (SELECT COUNT(*) FROM member) AS members,
    (SELECT COUNT(*) FROM photo_lab) AS photo_labs,
    (SELECT COUNT(*) FROM reservation) AS reservations,
    (SELECT COUNT(*) FROM post) AS posts,
    (SELECT COUNT(*) FROM comments) AS comments;
