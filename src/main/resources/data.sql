-- terms 테이블 기초 데이터 (ID 1, 2 생성)
INSERT INTO terms (
    type,
    version,
    title,
    content,
    is_required,
    is_active,
    effective_date,
    created_at,
    updated_at
) VALUES (
             'SERVICE',
             'v1.0',
             '서비스 이용약관',
             '서비스 이용약관의 상세 내용입니다.',
             true,
             true,
             '2026-01-01',
             NOW(),
             NOW()
         ), (
             'PRIVACY',
             'v1.0',
             '개인정보 처리방침',
             '개인정보 처리방침의 상세 내용입니다.',
             true,
             true,
             '2026-01-01',
             NOW(),
             NOW()
         );