# API Specification
마지막 업데이트: 2026-02-11

## Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080/api` |
| Dev | `https://dev-api.finders.it.kr/api` |
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

## API Endpoints

### Auth (인증)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| POST | `/auth/social/login` | 소셜 로그인(모바일 Native SDK) | None | `provider`, `accessToken` | 🔘 |
| POST | `/auth/social/login/code` | 소셜 로그인(웹 브라우저) | None | `provider`, `code` | 🔘 |
| POST | `/auth/owner/login` | 사장님 로그인 | None | `email`, `password` | ⚠️ |
| POST | `/auth/owner/signup` | 사장님 회원가입 | None | `email`, `password`, `name` | ⚠️ |
| POST | `/auth/reissue` | 토큰 재발급 | None | `refreshToken` (Cookie) | ❌ |
| POST | `/auth/logout` | 로그아웃 | USER/OWNER | `refreshToken` (Cookie) | ❌ |

### Dev Tools (개발용 도구)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/dev/login` | 서버용 토큰 발급 | None | `SecretKey` (Header), `memberId` | ✅ |

### Member (회원)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/members/me` | 마이페이지 조회 | USER, OWNER, ADMIN | - | ✅ |
| PATCH | `/members/me` | 내 정보 수정 | USER, OWNER, ADMIN | `nickname`, `profileImage` | ⚠️ |
| POST | `/members/social/signup/complete` | 소셜 회원가입 완료 | USER | `nickname`, `phone` | ⚠️ |
| POST | `/members/phone/verify/request` | 휴대폰 인증번호 요청 | None | `phone` | ⚠️ |
| POST | `/members/phone/verify/confirm` | 휴대폰 인증번호 확인 | None | `phone`, `code` | ⚠️ |
| GET | `/users/nickname/check` | 닉네임 중복 확인 | None | `nickname` | ✅ |
| DELETE | `/users/me` | 회원 탈퇴 | USER | - | 🔘 |

### User Address (회원 주소)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/users/addresses` | 배송지 목록 조회 | USER | - | ✅ |
| POST | `/users/addresses` | 배송지 추가 | USER | `address`, `isDefault` | ⚠️ |

### PhotoLab USER (현상소)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/photo-labs` | 현상소 목록 조회 | None | `page`, `size`, `sort` | ✅ |
| GET | `/photo-labs/popular` | 인기 현상소 조회 | None | - | ✅ |
| GET | `/photo-labs/regions` | 지역별 현상소 개수 조회 | None | - | ✅ |
| GET | `/photo-labs/notices` | 현상소 공지 조회 | None | `photoLabId` | ✅ |
| GET | `/photo-labs/favorites` | 관심 현상소 목록 조회 | USER | - | ✅ |
| GET | `/photo-labs/search` | 커뮤니티용 현상소 검색 | None | `keyword`, `locationAgreed` | ✅ |
| GET | `/photo-labs/search/autocomplete` | 검색어 자동완성 | None | `keyword` | ✅ |
| GET | `/photo-labs/search/preview` | 현상소 목록 preview | None | - | ✅ |
| GET | `/photo-labs/{photoLabId}` | 현상소 상세 조회 | None | - | ✅ |
| POST | `/photo-labs/{photoLabId}/favorites` | 즐겨찾기 추가 | USER | - | ✅ |
| DELETE | `/photo-labs/{photoLabId}/favorites` | 즐겨찾기 삭제 | USER | - | ✅ |

### PhotoLab Reservation (예약)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| POST | `/photo-labs/{photoLabId}/reservations` | 예약 등록 | USER | `date`, `time`, `type` | ⚠️ |
| GET | `/photo-labs/{photoLabId}/reservations/available-times` | 예약 가능 시간대 조회 | None | `date` | ⚠️ |
| GET | `/photo-labs/{photoLabId}/reservations/{reservationId}` | 예약 완료 조회 | USER | - | ✅ |
| DELETE | `/photo-labs/{photoLabId}/reservations/{reservationId}` | 예약 취소 | USER | - | 🔘 |

### Photo (회원 사진)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/photos/me/work-in-progress` | 진행중 작업 조회 | USER | - | ✅ |
| GET | `/photos/development-orders` | 현상 주문 목록 조회 | USER | - | ✅ |
| GET | `/photos/development-orders/{id}/scan-results` | 스캔 결과 사진 목록 | USER | - | ✅ |
| GET | `/photos/development-orders/{id}/photo-labs/account` | 현상소 계좌 조회 | USER | - | ⚠️ |
| POST | `/photos/development-orders/{id}/receipt` | 수령 확정 | USER | - | ⚠️ |
| POST | `/photos/development-orders/{id}/print/skip` | 인화 안함 확정 | USER | - | ✅ |
| POST | `/photos/print-orders` | 인화 주문 생성 | USER | `photoIds`, `options` | ⚠️ |
| GET | `/photos/print-orders/options` | 인화 옵션 목록 조회 | None | - | ✅ |
| POST | `/photos/print-orders/quote` | 인화 가격 조회 | USER | `photoIds`, `options` | ⚠️ |
| POST | `/photos/print-orders/{printOrderId}/deposit-receipt` | 입금 캡처 등록 | USER | `image` | ⚠️ |

### Photo Restoration (AI 사진 복원)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/restorations` | 복원 이력 조회 | USER | `page`, `size` | ✅ |
| POST | `/restorations` | 사진 복원 요청 (SUPIR) | USER | `originalPath`, `plan` | ✅ |
| GET | `/restorations/{restorationId}` | 복원 결과 조회 | USER | - | ✅ |
| POST | `/restorations/{restorationId}/feedback` | 복원 피드백 | USER | `rating`, `comment` | ✅ |
| POST | `/restorations/{restorationId}/share` | 복원 이미지 공유 | USER | - | ✅ |

### Community (사진 수다)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/posts` | 피드 목록 조회 | None | `page`, `size` | ✅ |
| POST | `/posts` | 게시물 작성 | USER | `content`, `images` | ⚠️ |
| GET | `/posts/preview` | 사진 미리보기 | None | - | ✅ |
| GET | `/posts/me` | 내가 쓴 글 목록 | USER | - | ✅ |
| GET | `/posts/likes` | 관심 게시글 목록 | USER | - | ✅ |
| GET | `/posts/search` | 게시글 검색 | None | `keyword` | ✅ |
| GET | `/posts/search/autocomplete` | 검색어 자동완성 | None | `keyword` | ✅ |
| GET | `/posts/search/history` | 최근 검색어 목록 | USER | - | ✅ |
| DELETE | `/posts/search/history/all` | 검색어 전체 삭제 | USER | - | ✅ |
| DELETE | `/posts/search/history/{searchHistoryId}` | 검색어 개별 삭제 | USER | - | ⚠️ |
| GET | `/posts/{postId}` | 게시물 상세 | None | - | ✅ |
| DELETE | `/posts/{postId}` | 게시글 삭제 | USER | - | ⚠️ |
| GET | `/posts/{postId}/comments` | 댓글 조회 | None | - | ✅ |
| POST | `/posts/{postId}/comments` | 댓글 작성 | USER | `content` | ✅ |
| DELETE | `/posts/comments/{commentId}` | 댓글 삭제 | USER | - | ✅ |
| POST | `/posts/{postId}/likes` | 좋아요 | USER | - | ✅ |
| DELETE | `/posts/{postId}/likes` | 좋아요 취소 | USER | - | ✅ |

### Payment (결제)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/payments` | 내 결제 목록 조회 | USER | - | ✅ |
| POST | `/payments/pre-register` | 결제 사전등록 | USER | `orderId`, `amount` | ⚠️ |
| POST | `/payments/complete` | 결제 완료 처리 | USER | `paymentKey`, `amount` | ⚠️ |
| GET | `/payments/{paymentId}` | 결제 상세 조회 | USER | - | ⚠️ |
| POST | `/payments/{paymentId}/cancel` | 결제 취소 | USER | `reason` | ⚠️ |

### File (파일)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| POST | `/files/presigned-url` | 업로드용 Presigned URL 발급 | USER/OWNER | `category`, `fileName`, `memberId` | ✅ |
| GET | `/files/signed-url` | Private 파일 조회 URL 발급 | USER/OWNER | `filePath` | ✅ |

### Inquiry USER (1:1 문의)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/inquiries` | 내 문의 목록 | USER | - | ✅ |
| POST | `/inquiries` | 문의 생성 | USER | `title`, `content`, `photoLabId`, `objectPaths` | ✅ |
| GET | `/inquiries/{inquiryId}` | 문의 상세 | USER | - | ✅ |

### Inquiry OWNER (현상소 문의)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/owner/inquiries` | 현상소 문의 목록 | OWNER | `photoLabId` | ✅ |
| GET | `/owner/inquiries/{inquiryId}` | 문의 상세 | OWNER | `photoLabId` | ✅ |
| POST | `/owner/inquiries/{inquiryId}/replies` | 답변 작성 | OWNER | `content` | ✅ |

### Inquiry ADMIN (관리자 문의)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| GET | `/admin/inquiries` | 서비스 문의 목록 | ADMIN | - | ✅ |
| GET | `/admin/inquiries/{inquiryId}` | 문의 상세 | ADMIN | - | ✅ |
| POST | `/admin/inquiries/{inquiryId}/replies` | 답변 작성 | ADMIN | `content` | ✅ |

### PhotoLab OWNER (현상소 관리)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| POST | `/owner/photo-labs` | 현상소 기본사항 등록 | OWNER | `name`, `address`, `phone` | ⚠️ |
| POST | `/owner/photo-labs/images/presigned-url` | 이미지 업로드 URL 발급 | OWNER | `fileName`, `photoLabId` | ⚠️ |
| POST | `/owner/photo-labs/images` | 이미지 등록 | OWNER | `imageUrl` | 🔘 |
| POST | `/owner/photo-labs/documents/presigned-url` | 서류 업로드 URL 발급 | OWNER | `fileName`, `photoLabId` | ⚠️ |
| POST | `/owner/photo-labs/documents` | 사업자 서류 등록 | OWNER | `documentUrl` | 🔘 |

### Owner Photo (오너 사진 관리)
| Method | Path | Summary | Auth | Key Parameters | Status |
|--------|------|---------|------|----------------|--------|
| POST | `/owner/photo-labs/{photoLabId}/development-orders` | 현상 주문 생성 | OWNER | `memberId`, `orderType` | ⚠️ |
| PATCH | `/owner/photo-labs/{photoLabId}/development-orders/{id}/status` | 주문 상태 변경 | OWNER | `status` | ⚠️ |
| POST | `/owner/photo-labs/{photoLabId}/scan-photos/presigned-urls` | 스캔 이미지 URL 벌크 발급 | OWNER | `count` | ⚠️ |
| POST | `/owner/photo-labs/{photoLabId}/development-orders/{id}/scanned-photos` | 스캔 이미지 메타 등록 | OWNER | `photoUrls` | 🔘 |
| PATCH | `/owner/photo-labs/{photoLabId}/print-orders/{id}/status` | 인화 주문 완료 | OWNER | `status` | 🔘 |
| PATCH | `/owner/photo-labs/{photoLabId}/print-orders/{id}/printing` | 예상 완료 시간 등록 | OWNER | `estimatedTime` | 🔘 |
| PATCH | `/owner/photo-labs/{photoLabId}/print-orders/{id}/shipping` | 배송 정보 등록 | OWNER | `trackingNumber` | 🔘 |

## Dev Server Test Results

**테스트 일자**: 2026-02-11

**전체 엔드포인트**: 91개

**결과 요약**:
- ✅ **Passed**: 52개 (200/201 정상 응답 확인)
- ⚠️ **Expected Error**: 27개 (검증/비즈니스 규칙 에러 — 엔드포인트 정상 작동, 테스트 데이터/파라미터 부족)
- ❌ **Server Error**: 2개 (예상치 못한 500 에러 — 조사 필요)
  - POST `/auth/logout` — COMMON_500 (refreshToken 쿠키 미전달로 인한 오류 가능성)
  - POST `/auth/reissue` — COMMON_500 (refreshToken 쿠키 미전달로 인한 오류 가능성)
- 🔘 **Not Testable**: 10개 (실제 OAuth/SMS/파일 업로드/결제/인화 주문 등 필요)

**주요 수정 이력**:
- PR #442: `GET /restorations` Sort 파싱 버그 수정 (`PageRequest.of()` 방식)
- PR #442: `POST /restorations` SUPIR `s_stage1` 타입 버그 수정 (`Double → Integer`)
- PR #443: Sort 파라미터 `@RequestParam` 방식 전환 + 코드리뷰 반영
- PR #445: CD Docker pull 누락 수정 (`--profile blue --profile green`)
- PR #448: Webhook SUPIR output 파싱 수정 (`List<String> → Object` + `instanceof` 분기)

## Error Codes

### Common (COMMON_xxx)
| Code | Status | Message |
|------|--------|---------|
| COMMON_400 | 400 | 잘못된 요청입니다. |
| COMMON_401 | 401 | 인증이 필요합니다. |
| COMMON_403 | 403 | 접근 권한이 없습니다. |
| COMMON_404 | 404 | 요청한 리소스를 찾을 수 없습니다. |
| COMMON_405 | 405 | 허용되지 않은 HTTP 메서드입니다. |
| COMMON_409 | 409 | 리소스 충돌이 발생했습니다. |
| COMMON_500 | 500 | 서버 내부 오류가 발생했습니다. |

### Validation (VALID_xxx)
| Code | Status | Message |
|------|--------|---------|
| VALID_400 | 400 | 입력값이 올바르지 않습니다. |
| VALID_401 | 400 | 필수 파라미터가 누락되었습니다. |
| VALID_402 | 400 | 파라미터 타입이 올바르지 않습니다. |

### Auth (AUTH_xxx)
| Code | Status | Message |
|------|--------|---------|
| AUTH_401 | 401 | 인증 토큰이 존재하지 않습니다. |
| AUTH_402 | 401 | 유효하지 않은 토큰입니다. |
| AUTH_403 | 401 | 만료된 토큰입니다. |
| AUTH_410 | 400 | 지원하지 않는 소셜 로그인 제공자입니다. |
| AUTH_411 | 400 | 소셜 로그인 요청 정보가 올바르지 않습니다. |
| AUTH_430 | 403 | 해당 서비스에 접근 권한이 없는 계정 타입입니다. |
| AUTH_400 | 400 | 소셜 로그인에 실패했습니다. |
| AUTH_404 | 400 | 소셜 토큰 발급에 실패했습니다. |
| AUTH_405 | 400 | 소셜 사용자 정보를 불러오지 못했습니다. |
| AUTH_406 | 401 | 가입되지 않은 소셜 계정입니다. |
| AUTH_409 | 409 | 다른 소셜 계정으로 가입된 회원입니다. |
| AUTH_412 | 403 | 이용이 제한된 계정입니다. |
| AUTH_413 | 403 | 필수 약관에 동의하지 않았습니다. |
| AUTH_414 | 403 | 추가 정보 입력이 필요합니다. |
| AUTH_415 | 400 | 인증번호 요청이 너무 많습니다. |
| AUTH_420 | 400 | 인증번호가 올바르지 않습니다. |
| AUTH_421 | 400 | 인증번호가 만료되었거나 존재하지 않습니다. |
| AUTH_422 | 429 | 인증 시도 횟수를 초과했습니다. |
| AUTH_423 | 409 | 이미 인증이 완료된 요청입니다. |
| AUTH_503 | 503 | 현재 인증 서비스 이용이 원활하지 않습니다. 잠시 후 다시 시도해주세요. |

> **참고**: `AUTH_401`은 두 가지 경우에 사용됩니다: 인증 토큰 미존재 또는 이메일/비밀번호 불일치 (사장님 로그인).

### Member (MEMBER_xxx)
| Code | Status | Message |
|------|--------|---------|
| MEMBER_404 | 404 | 회원을 찾을 수 없습니다. |
| MEMBER_409 | 409 | 이미 존재하는 회원입니다. |
| MEMBER_410 | 409 | 이미 사용 중인 닉네임입니다. |
| MEMBER_411 | 409 | 이미 사용 중인 이메일입니다. |
| MEMBER_402 | 403 | 비활성화되었거나 이미 탈퇴한 계정입니다. |
| MEMBER_420 | 400 | 휴대폰 인증이 필요합니다. |
| MEMBER_421 | 400 | 휴대폰 인증에 실패했습니다. |
| MEMBER_430 | 403 | 필수 약관에 동의하지 않았습니다. |
| MEMBER_440 | 403 | 방문 예정인 예약 내역이 있습니다. 예약 취소 후 탈퇴가 가능합니다. |
| MEMBER_441 | 403 | 진행 중인 현상/인화 작업이 있습니다. 완료 후 시도해 주세요. |
| MEMBER_442 | 403 | 답변 대기 중인 문의가 있습니다. 답변 확인 후 탈퇴가 가능합니다. |

### Store (STORE_xxx)
| Code | Status | Message |
|------|--------|---------|
| STORE_404 | 404 | 현상소를 찾을 수 없습니다. |
| STORE_403 | 403 | 해당 현상소에 접근 권한이 없습니다. |
| REGION_404 | 404 | 지역을 찾을 수 없습니다. |
| BUSINESS_HOUR_404 | 404 | 현상소의 영업시간을 찾을 수 없습니다. |

### Reservation (RESERVATION_xxx)
| Code | Status | Message |
|------|--------|---------|
| RESERVATION_404 | 404 | 예약을 찾을 수 없습니다. |
| RESERVATION_409 | 409 | 해당 시간에 이미 예약이 있습니다. |
| RESERVATION_400 | 400 | 예약을 취소할 수 없습니다. |
| RESERVATION_409_FULL | 409 | 해당 시간대의 예약이 모두 마감되었습니다. |
| RESERVATION_SLOT_404 | 404 | 예약 슬롯을 찾을 수 없습니다. |

### Photo (PHOTO_xxx)
| Code | Status | Message |
|------|--------|---------|
| PHOTO_404 | 404 | 사진을 찾을 수 없습니다. |
| PHOTO_500 | 500 | 사진 업로드에 실패했습니다. |
| PHOTO_501 | 500 | 사진 복구에 실패했습니다. |
| PHOTO_400_FILES_REQUIRED | 400 | 스캔 이미지 파일은 필수입니다. |
| PHOTO_403_OWNER_MISMATCH | 403 | 해당 현상소의 오너가 아닙니다. |
| PHOTO_400_RESERVATION_MISMATCH | 400 | 해당 현상소의 예약이 아닙니다. |
| PHOTO_409_ORDER_EXISTS | 409 | 이미 해당 예약으로 현상 주문이 생성되었습니다. |
| PHOTO_400_MEMBER_REQUIRED | 400 | 현장 접수 시 회원 정보가 필요합니다. |
| PHOTO_404_ORDER_NOT_FOUND | 404 | 현상 주문을 찾을 수 없습니다. |
| PHOTO_400_ORDER_PHOTOLAB_MISMATCH | 400 | 해당 현상소의 주문이 아닙니다. |
| PHOTO_400_PHOTOLAB_ACCOUNT_NOT_REGISTERED | 400 | 현상소의 사업자 계좌 정보가 등록되어 있지 않습니다. |
| PHOTO_403_PHOTOLAB_ACCOUNT_ACCESS_DENIED | 403 | 해당 주문에 대한 현상소 계좌 정보에 접근할 수 없습니다. |
| PHOTO_404_PRINT_ORDER_NOT_FOUND | 404 | 해당 인화 주문을 찾을 수 없습니다. |
| PHOTO_400_PRINT_ORDER_STATUS_INVALID | 400 | 현재 인화 주문 상태에서는 해당 작업을 수행할 수 없습니다. |
| PHOTO_403_PRINT_ORDER_OWNER_MISMATCH | 403 | 해당 인화 주문을 처리할 권한이 없습니다. |
| PHOTO_409_PAYMENT_ALREADY_SUBMITTED | 409 | 이미 입금 증빙이 제출된 주문입니다. |
| PHOTO_409_DELIVERY_ALREADY_CREATED | 409 | 이미 배송 정보가 등록된 주문입니다. |
| PHOTO_404_DELIVERY_NOT_FOUND | 404 | 배송 정보를 찾을 수 없습니다. |
| PHOTO_400_DELIVERY_STATUS_INVALID | 400 | 현재 배송 상태에서는 해당 작업을 수행할 수 없습니다. |
| PHOTO_400_PRINT_ORDER_INVALID | 400 | 현재 인화 주문 상태에서는 해당 작업을 수행할 수 없습니다. |

### Credit (CREDIT_xxx)
| Code | Status | Message |
|------|--------|---------|
| CREDIT_402 | 402 | 크레딧이 부족합니다. |
| CREDIT_404 | 404 | 크레딧 정보를 찾을 수 없습니다. |

### Payment (PAYMENT_xxx)
| Code | Status | Message |
|------|--------|---------|
| PAYMENT_404 | 404 | 결제 정보를 찾을 수 없습니다. |
| PAYMENT_409 | 409 | 이미 존재하는 결제 ID입니다. |
| PAYMENT_403 | 403 | 해당 결제에 접근 권한이 없습니다. |
| PAYMENT_410 | 409 | 이미 처리된 결제입니다. |
| PAYMENT_400 | 400 | 결제 금액이 일치하지 않습니다. |
| PAYMENT_401 | 400 | 처리할 수 없는 결제 상태입니다. |
| PAYMENT_402 | 400 | 취소할 수 없는 결제 상태입니다. |
| PAYMENT_500 | 500 | 결제 취소에 실패했습니다. |
| PAYMENT_412 | 400 | 취소 금액이 결제 금액을 초과할 수 없습니다. |
| PAYMENT_411 | 400 | 웹훅 검증에 실패했습니다. |

### External API (EXTERNAL_xxx)
| Code | Status | Message |
|------|--------|---------|
| EXTERNAL_503 | 503 | 외부 API 호출에 실패했습니다. |
| KAKAO_502 | 502 | 카카오 약관 동의 내역을 불러오는 중 오류가 발생했습니다. |
| KAKAO_500 | 400 | 카카오 연결 끊기 중 오류가 발생했습니다. |
| KAKAO_401 | 401 | 카카오 인증 정보가 만료되었거나 유효하지 않습니다. |
| KAKAO_502 | 502 | 카카오 서버 오류로 약관 정보를 불러오지 못했습니다. |

### Storage (STORAGE_xxx)
| Code | Status | Message |
|------|--------|---------|
| STORAGE_500 | 500 | 파일 업로드에 실패했습니다. |
| STORAGE_501 | 500 | 파일 삭제에 실패했습니다. |
| STORAGE_404 | 404 | 파일을 찾을 수 없습니다. |
| STORAGE_400 | 400 | 잘못된 저장 경로입니다. |
| STORAGE_401 | 400 | 허용되지 않는 파일 형식입니다. |
| STORAGE_402 | 400 | 파일 크기가 제한을 초과했습니다. |
| STORAGE_502 | 500 | Signed URL 생성에 실패했습니다. |
| STORAGE_403 | 400 | 해당 API에서 지원하지 않는 업로드 카테고리입니다. |
| STORAGE_405 | 403 | 해당 경로에 대한 업로드 권한이 없습니다. |

### Inquiry (INQUIRY_xxx)
| Code | Status | Message |
|------|--------|---------|
| INQUIRY_404 | 404 | 문의를 찾을 수 없습니다. |
| INQUIRY_403 | 403 | 해당 문의에 접근 권한이 없습니다. |
| INQUIRY_400 | 400 | 이미 종료된 문의입니다. |

### Community (COMMUNITY_xxx)
| Code | Status | Message |
|------|--------|---------|
| COMMUNITY_401 | 400 | 리뷰는 최소 20자 이상 작성해야 합니다. |
| COMMUNITY_402 | 400 | 리뷰는 최대 300자 이내로 작성해야 합니다. |

