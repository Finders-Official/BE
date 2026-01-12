# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Finders API - 필름 현상소 연결 플랫폼 백엔드. Java 21 + Spring Boot 3.4 + MySQL 8.x 기반.

## Build & Run Commands

```bash
# Local development (requires Docker for MySQL)
docker compose up -d          # Start MySQL container
./gradlew bootRun             # Run application (default: local profile)
docker compose down           # Stop MySQL
docker compose down -v        # Stop and reset data

# Build
./gradlew build               # Full build with tests
./gradlew build -x test       # Build without tests
./gradlew clean build         # Clean build

# Test
./gradlew test                              # All tests
./gradlew test --tests "ClassName"          # Single class
./gradlew test --tests "ClassName.method"   # Single method
```

## Architecture

도메인 기반 계층형 아키텍처 (Package by Feature + Layered Architecture)

```
src/main/java/com/finders/api/
├── domain/           # 도메인별 비즈니스 로직
│   └── {domain}/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── dto/
│       └── enums/
├── infra/            # 외부 서비스 연동 (OAuth, Google Cloud, Storage)
└── global/           # 공통 모듈 (config, response, exception)
```

**Domains**: member, auth, store, reservation, photo, community, inquiry

## Code Conventions

### Naming
- Entity: no suffix (e.g., `Member`)
- DTO: `{Domain}Request`, `{Domain}Response` with nested static classes (`Create`, `Update`, `Detail`, `Summary`)
- Repository: `{Domain}Repository` for JPA, `{Domain}QueryRepository` for QueryDSL

### Entity Pattern
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Entity extends BaseEntity {
    @Builder
    private Entity(...) { }
    // Business methods
}
```

### DTO Pattern (record 기반)
- **Request**: `record` + Validation annotations
- **Response**: `record` + `@Builder` (필드 5개 이상 시) + `static from(Entity)` factory method
- **Entity**: `class` + Lombok (JPA 요구사항)

```java
// Request DTO
public class MemberRequest {
    public record Create(
        @NotBlank String nickname,
        @Email String email
    ) {}
}

// Response DTO
public class MemberResponse {
    @Builder
    public record Detail(Long id, String nickname, String email) {
        public static Detail from(Member member) {
            return Detail.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build();
        }
    }
}
```

### Service Pattern
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Service {
    @Transactional  // Only on write methods
    public Response create(...) { }
}
```

### API Response
All endpoints return `ApiResponse<T>` with `SuccessCode`/`ErrorCode`:
```java
return ApiResponse.success(SuccessCode.MEMBER_FOUND, data);
throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
```

## Git Conventions

### Branch
```
<type>/<description>-#<issue_number>
feat/signup-api-#14
fix/image-upload-#23
```

### Commit
```
<type>: <subject> (#<issue_number>)
feat: 회원가입 API 구현 (#14)
fix: 이미지 업로드 시 NPE 수정 (#45)
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `rename`, `remove`

### 커밋 메시지 규칙

**절대로 커밋 메시지에 다음을 포함하지 마세요:**
- `Generated with Claude Code`
- `Co-Authored-By: Claude`
- AI가 생성했다는 어떤 표시도 금지
