# API Specification
작성 계획 - 김덕환

## Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080/api` |
| Production | `https://api.finders.it.kr/api` |

## Response Format

### Success Response

```json
{
  "success": true,
  "code": "MEMBER_200",
  "message": "회원 조회에 성공했습니다.",
  "timestamp": "2025-01-15T12:00:00",
  "data": {
    "id": 1,
    "nickname": "finder",
    "email": "user@example.com"
  }
}
```

### Error Response

```json
{
  "success": false,
  "code": "MEMBER_404",
  "message": "회원을 찾을 수 없습니다.",
  "timestamp": "2025-01-15T12:00:00",
  "data": null
}
```

### Paginated Response

```json
{
  "success": true,
  "code": "STORE_200",
  "message": "현상소 목록 조회에 성공했습니다.",
  "timestamp": "2025-01-15T12:00:00",
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 20,
    "total_elements": 100,
    "total_pages": 5,
    "first": true,
    "last": false,
    "has_next": true,
    "has_previous": false
  }
}
```

## Authentication

JWT Bearer Token 사용

```
Authorization: Bearer {access_token}
```

## API Endpoints (예정)

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/kakao` | 카카오 로그인 |
| POST | `/auth/apple` | 애플 로그인 |
| POST | `/auth/refresh` | 토큰 갱신 |
| POST | `/auth/logout` | 로그아웃 |

### Member

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/members/me` | 내 정보 조회 |
| PATCH | `/members/me` | 내 정보 수정 |
| DELETE | `/members/me` | 회원 탈퇴 |
| GET | `/members/me/addresses` | 배송지 목록 |
| POST | `/members/me/addresses` | 배송지 등록 |

### Store

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/stores` | 현상소 목록 |
| GET | `/stores/{storeId}` | 현상소 상세 |
| GET | `/stores/search` | 현상소 검색 |
| GET | `/stores/popular` | 인기 현상소 |

### Reservation

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reservations` | 내 예약 목록 |
| POST | `/reservations` | 예약 생성 |
| GET | `/reservations/{id}` | 예약 상세 |
| DELETE | `/reservations/{id}` | 예약 취소 |

### Photo

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/photos` | 내 사진 목록 |
| GET | `/photos/{id}` | 사진 상세 |
| POST | `/photos/{id}/restore` | AI 사진 복구 |
| POST | `/photos/{id}/print` | 인화 요청 |

### Community

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/posts` | 피드 목록 |
| POST | `/posts` | 게시글 작성 |
| GET | `/posts/{id}` | 게시글 상세 |
| POST | `/posts/{id}/like` | 좋아요 |

### Inquiry (User)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/inquiries` | 내 문의 목록 |
| GET | `/api/v1/inquiries/{id}` | 문의 상세 |
| POST | `/api/v1/inquiries` | 문의 생성 (photoLabId null=고객센터, 있으면=매장문의) |

### Inquiry (Owner)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/owner/inquiries` | 현상소 문의 목록 |
| GET | `/api/v1/owner/inquiries/{id}` | 문의 상세 |
| POST | `/api/v1/owner/inquiries/{id}/replies` | 답변 작성 |

### Inquiry (Admin)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/inquiries` | 서비스 문의 목록 |
| GET | `/api/v1/admin/inquiries/{id}` | 문의 상세 |
| POST | `/api/v1/admin/inquiries/{id}/replies` | 답변 작성 |

## Error Codes

### Common (COMMON_xxx)
| Code | Status | Message |
|------|--------|---------|
| COMMON_400 | 400 | 잘못된 요청입니다. |
| COMMON_401 | 401 | 인증이 필요합니다. |
| COMMON_403 | 403 | 접근 권한이 없습니다. |
| COMMON_404 | 404 | 요청한 리소스를 찾을 수 없습니다. |
| COMMON_500 | 500 | 서버 내부 오류가 발생했습니다. |

### Auth (AUTH_xxx)
| Code | Status | Message |
|------|--------|---------|
| AUTH_401 | 401 | 유효하지 않은 토큰입니다. |
| AUTH_402 | 401 | 만료된 토큰입니다. |
| AUTH_400 | 400 | 소셜 로그인에 실패했습니다. |

### Member (MEMBER_xxx)
| Code | Status | Message |
|------|--------|---------|
| MEMBER_404 | 404 | 회원을 찾을 수 없습니다. |
| MEMBER_409 | 409 | 이미 존재하는 회원입니다. |

### Store (STORE_xxx)
| Code | Status | Message |
|------|--------|---------|
| STORE_404 | 404 | 현상소를 찾을 수 없습니다. |

### Reservation (RESERVATION_xxx)
| Code | Status | Message |
|------|--------|---------|
| RESERVATION_404 | 404 | 예약을 찾을 수 없습니다. |
| RESERVATION_409 | 409 | 해당 시간에 이미 예약이 있습니다. |
