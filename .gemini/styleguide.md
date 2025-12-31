# Finders API Review Guide

## Review Style

- 일반적인 피드백, 요약, 변경 설명, 칭찬은 피한다.
- 구체적이고 객관적인 인사이트만 제공한다.
- 시스템 영향에 대한 광범위한 코멘트나 의도 질문은 하지 않는다.
- **모든 코멘트는 한국어(ko-KR)로 작성한다.**

---

## Architecture

이 프로젝트는 **DDD(Domain-Driven Design)** 기반의 계층형 아키텍처를 사용한다.

### Layer Structure

```
Controller → Service → Repository → Entity
```

- **Controller**: API 진입점, 요청/응답 처리
- **Service**: 비즈니스 로직 처리, `@Transactional` 관리
- **Repository**: 데이터 접근, QueryDSL 사용
- **Entity**: 도메인 모델, JPA 엔티티

### Package Structure

```
com.finders.api/
├── domain/{domain}/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── enums/
├── infra/           # 외부 시스템 (OAuth, GCP 등)
└── global/          # 공통 모듈 (config, exception, response)
```

### Layer Rules

- **상위 레이어**에서만 **하위 레이어**를 참조할 수 있다.
- Controller → Service → Repository 순서를 지킨다.
- 동일 레이어 간 참조는 금지한다.

---

## Naming Conventions

### Package

- 소문자만 사용
- 단수형 사용

```java
// Good
com.finders.api.domain.member

// Bad
com.finders.api.domain.Members
```

### Class

| Type | Suffix | Example |
|------|--------|---------|
| Controller | `Controller` | `MemberController` |
| Service | `Service` | `MemberService` |
| Repository (JPA) | `Repository` | `MemberRepository` |
| Repository (QueryDSL) | `QueryRepository` | `MemberQueryRepository` |
| Entity | (없음) | `Member` |
| DTO | `Request`, `Response` | `MemberRequest`, `MemberResponse` |
| Exception | `Exception` | `CustomException` |
| Config | `Config` | `SecurityConfig` |

### Method

- camelCase 사용
- 동사로 시작

| Action | Prefix | Example |
|--------|--------|---------|
| 조회 (단건) | `get`, `find` | `getMember()`, `findById()` |
| 조회 (목록) | `get`, `find`, `search` | `getMembers()`, `searchStores()` |
| 생성 | `create`, `save` | `createMember()` |
| 수정 | `update`, `modify` | `updateMember()` |
| 삭제 | `delete`, `remove` | `deleteMember()` |
| 검증 | `validate`, `check`, `is` | `validateEmail()`, `isExist()` |

### Variable

- camelCase 사용
- 의미 있는 이름 사용

```java
// Good
Member member;
List<Store> stores;
Long memberId;

// Bad
Member m;
List<Store> list;
Long id;
```

---

## Code Style

### Base Entity

모든 Entity는 `BaseTimeEntity` 또는 `BaseEntity`를 상속한다.

```java
// 생성일/수정일만 필요한 경우
@MappedSuperclass
public abstract class BaseTimeEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

// Soft Delete가 필요한 경우
@MappedSuperclass
public abstract class BaseEntity extends BaseTimeEntity {
    private LocalDateTime deletedAt;

    public void softDelete() { this.deletedAt = LocalDateTime.now(); }
    public boolean isDeleted() { return this.deletedAt != null; }
    public void restore() { this.deletedAt = null; }
}
```

**상속 규칙:**
- `BaseTimeEntity`: 단순 생성일/수정일만 필요한 경우 (예: `Comment`, `PostLike`)
- `BaseEntity`: Soft Delete가 필요한 경우 (예: `Member`, `Post`, `PhotoLab`)

### Entity

```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    // 생성 메서드 (@Builder는 private 생성자에)
    @Builder
    private Member(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
        this.status = MemberStatus.ACTIVE;
    }

    // 비즈니스 메서드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

**Entity 규칙:**
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- `@Setter` 사용 금지, 비즈니스 메서드로 상태 변경
- `@Builder`는 private 생성자에 적용
- `BaseEntity` 또는 `BaseTimeEntity` 상속
- Enum 필드는 `@Enumerated(EnumType.STRING)` 필수

### Enum

```java
// 도메인별 enums 패키지에 정의
public enum MemberStatus {
    ACTIVE,
    SUSPENDED,
    WITHDRAWN
}

public enum MemberRole {
    USER,
    OWNER,
    ADMIN
}
```

**Enum 규칙:**
- UPPER_SNAKE_CASE 사용
- Entity에서 `@Enumerated(EnumType.STRING)` 필수 (ORDINAL 금지)
- 도메인별 `enums/` 패키지에 위치

### DTO (record 기반)

**record vs class 사용 기준:**

| 구분 | 권장 | 이유 |
|------|------|------|
| **Request/Response DTO** | `record` | 불변, 간결, 순수 Java |
| **Entity** | `class + Lombok` | JPA 기본 생성자 요구 |

```java
// Request DTO (record)
public class MemberRequest {

    public record Create(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20)
        String nickname,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
    ) {}
}
```

```java
// Response DTO (record + @Builder)
public class MemberResponse {

    @Builder
    public record Detail(
        Long id,
        String nickname,
        String email
    ) {
        public static Detail from(Member member) {
            return Detail.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .build();
        }
    }

    // 필드 3개 이하면 생성자 직접 사용
    public record Summary(Long id, String nickname) {
        public static Summary from(Member member) {
            return new Summary(member.getId(), member.getNickname());
        }
    }
}
```

**DTO 규칙:**
- Request/Response를 외부 클래스로, 세부 DTO를 `record`로 정의
- 필드 5개 이상이면 `@Builder` 사용, 3개 이하면 생성자 직접 사용
- Response에는 `from()` 정적 팩토리 메서드 사용
- 검증 어노테이션에 한글 message 포함

### Controller

```java
@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 조회")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse.Detail> getMember(
            @PathVariable Long memberId
    ) {
        return ApiResponse.success(
                SuccessCode.MEMBER_FOUND,
                memberService.getMember(memberId)
        );
    }

    @Operation(summary = "현상소 목록 조회")
    @GetMapping
    public PagedResponse<StoreResponse.Summary> getStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return storeService.getStores(PageRequest.of(page, size));
    }
}
```

**Controller 규칙:**
- `@Tag`, `@Operation` 어노테이션으로 Swagger 문서화
- 단건 응답: `ApiResponse<T>` 사용
- 목록 응답: `PagedResponse<T>` 사용
- `SuccessCode`/`ErrorCode` enum 사용

### Service

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse.Detail getMember(Long memberId) {
        log.info("[MemberService.getMember] memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.Detail.from(member);
    }

    @Transactional
    public MemberResponse.Detail updateMember(Long memberId, MemberRequest.Update request) {
        log.info("[MemberService.updateMember] memberId: {}", memberId);
        // 수정 로직
    }
}
```

**Service 규칙:**
- 클래스에 `@Transactional(readOnly = true)` 기본 적용
- 수정/삭제 메서드에만 `@Transactional` 추가
- `@Slf4j`로 로깅, `[클래스명.메서드명]` 형식 사용
- `CustomException`으로 예외 처리
- `orElseThrow()` 패턴 사용 (`.get()` 금지)

### Repository

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT m FROM Member m WHERE m.status = :status")
    List<Member> findAllByStatus(@Param("status") MemberStatus status);
}
```

### QueryDSL Repository

```java
@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Member> searchMembers(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .where(
                        nicknameContains(condition.getNickname()),
                        statusEq(condition.getStatus())
                )
                .orderBy(member.createdAt.desc())
                .fetch();
    }

    // BooleanExpression 헬퍼 메서드 (null 반환 시 조건 무시)
    private BooleanExpression nicknameContains(String nickname) {
        return StringUtils.hasText(nickname)
                ? member.nickname.contains(nickname)
                : null;
    }

    private BooleanExpression statusEq(MemberStatus status) {
        return status != null ? member.status.eq(status) : null;
    }
}
```

**QueryDSL 규칙:**
- 클래스명: `{Domain}QueryRepository`
- 동적 쿼리용 private 메서드로 `BooleanExpression` 반환
- null 반환으로 조건 무시 처리
- `StringUtils.hasText()` 사용하여 빈 문자열도 체크

---

## Response Patterns

### 단건 응답 (ApiResponse)

```java
// 성공 응답
return ApiResponse.success(SuccessCode.MEMBER_FOUND, memberResponse);

// 데이터 없는 성공 응답
return ApiResponse.success(SuccessCode.MEMBER_DELETED);

// 에러 응답 (보통 GlobalExceptionHandler에서 처리)
return ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND);
```

### 목록 응답 (PagedResponse)

```java
// Entity를 그대로 반환할 때
Page<Store> storePage = storeRepository.findAll(pageable);
return PagedResponse.of(SuccessCode.STORE_LIST_FOUND, storePage);

// DTO로 변환할 때
Page<Store> storePage = storeRepository.findAll(pageable);
List<StoreResponse.Summary> dtoList = storePage.getContent().stream()
        .map(StoreResponse.Summary::from)
        .toList();
return PagedResponse.of(SuccessCode.STORE_LIST_FOUND, dtoList, storePage);
```

---

## Exception Handling

```java
// 예외 발생
throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);

// 메시지 커스텀
throw new CustomException(ErrorCode.INVALID_INPUT, "닉네임 형식이 올바르지 않습니다.");

// Optional 처리 (권장 패턴)
Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

// ErrorCode enum 정의
MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "회원을 찾을 수 없습니다.")
```

---

## Logging Format

```java
// 메서드 진입 로깅
log.info("[MemberService.getMember] memberId: {}", memberId);

// 중요 이벤트 로깅
log.info("[MemberService.createMember] 회원 생성 완료: memberId={}", member.getId());

// 에러 로깅 (GlobalExceptionHandler에서)
log.error("[CustomException] code: {}, message: {}", errorCode.getCode(), e.getMessage());
```

---

## Common Anti-Patterns

다음 패턴은 리뷰에서 지적해야 한다:

1. **Entity에 @Setter 사용** - 비즈니스 메서드로 대체
2. **Controller에서 직접 Repository 호출** - Service 레이어 통해 접근
3. **Service에서 Response 타입 반환 누락** - DTO로 변환하여 반환
4. **@Transactional 누락** - 수정/삭제 메서드에 필수
5. **의미 없는 변수명** - m, list, id 등 사용 금지
6. **Optional.get() 직접 호출** - `orElseThrow()` 사용
7. **동일 레이어 간 참조** - Controller→Controller, Service→Service 금지
8. **raw 타입 사용** - 제네릭 타입 명시
9. **매직 넘버/스트링** - 상수 또는 Enum 사용
10. **긴 메서드** - 단일 책임 원칙(SRP)에 따라 분리
11. **@Enumerated(EnumType.ORDINAL)** - STRING만 사용
12. **BaseEntity 미상속** - 모든 Entity는 Base 상속 필수
13. **ApiResponse 미사용** - 통일된 응답 구조 필수
14. **로그 포맷 미준수** - `[클래스명.메서드명]` 형식 사용

---

## Git Conventions

### Branch Naming

```
<type>/<설명>-#<issue_number>
```

예: `feat/signup-api-#14`, `fix/image-upload-#23`

### Commit Message

```
<type>: <subject> (#<issue_number>)
```

| Type | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드 |
| `chore` | 빌드/설정 변경 |
