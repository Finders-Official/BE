# Code Style Guide

Java 21 & Spring Boot 3.4 코드 작성 스타일 가이드입니다.

---

## 핵심 기술 스택

| 기술 | 적용 | 이유 |
|------|------|------|
| **record DTO** | ✅ | 불변성 보장, 보일러플레이트 제거 |
| **Virtual Threads** | ✅ | 동시성 처리 성능 향상 |
| **FixtureMonkey** | ✅ | 테스트 데이터 자동 생성 |

### Virtual Threads 설정

```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true
```

---

## record vs class + Lombok 사용 기준

### 한눈에 보기

| 구분 | 권장 | 이유 |
|------|------|------|
| **Request/Response DTO** | `record` | 불변, 간결, 순수 Java |
| **Entity** | `class + Lombok` | JPA 기본 생성자 요구 |
| **Service/Controller** | `Lombok` | @RequiredArgsConstructor |
| **Config** | `Lombok` | @RequiredArgsConstructor |

### 불변성이란?

**한번 생성되면 값을 바꿀 수 없다**는 의미입니다.

```java
// record는 setter가 없음
MemberResponse response = new MemberResponse(1L, "홍길동");
response.setName("김철수");  // ❌ 컴파일 에러 - 이런 메서드 자체가 없음

// 값을 바꾸고 싶으면 새 객체를 만들어야 함
MemberResponse updated = new MemberResponse(1L, "김철수");
```

### 왜 DTO는 record, Entity는 class인가?

```java
// Entity - 값 변경 가능해야 함 (비즈니스 로직)
member.updateNickname("새닉네임");  // ✅ 필요함

// DTO - 값 변경 불필요 (데이터 운반용)
// API 요청 받고 → 응답 보내면 끝. 중간에 값 바꿀 일 없음
```

### record + @Builder 언제 쓰나?

```java
// 필드가 5개 이상이거나 선택적 필드가 많을 때
@Builder
public record MemberDetailResponse(
    Long id,
    String nickname,
    String email,
    String phone,
    String profileImage,
    LocalDateTime createdAt
) {
    public static MemberDetailResponse from(Member member) {
        return MemberDetailResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                // ...
                .build();
    }
}

// 필드가 3개 이하면 그냥 생성자 사용
public record MemberSummary(Long id, String nickname) {
    public static MemberSummary from(Member member) {
        return new MemberSummary(member.getId(), member.getNickname());
    }
}
```

### Lombok은 어디서 쓰이나?

```java
// Entity - 필수 (JPA가 기본 생성자 요구)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member { }

// Service, Controller - @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;  // 자동 주입
}

// Config 클래스
@Configuration
@RequiredArgsConstructor
public class SecurityConfig { }
```

> **결론:** Lombok은 프로젝트에서 계속 사용합니다. DTO에서만 record로 대체하는 것입니다.

---

## 어노테이션 컨벤션

> 모든 도메인에서 동일한 어노테이션 순서와 패턴을 사용합니다.

### 순서 원칙

```
1. 스테레오타입 (@Entity, @Service, @RestController 등)
2. 설정 (@Table, @RequestMapping 등)
3. Lombok (@Getter, @Builder, @RequiredArgsConstructor 등)
```

---

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

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)      // STRING 필수
    @Column(nullable = false)
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY 필수
    @JoinColumn(name = "store_id")
    private Store store;

    // @Builder는 private 생성자에만
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

| 어노테이션 | 필수 | 비고 |
|-----------|------|------|
| `@Entity`, `@Table` | O | |
| `@Getter` | O | `@Setter` 금지 |
| `@NoArgsConstructor(access = PROTECTED)` | O | |
| `@Builder` | O | private 생성자에만 |
| `@Enumerated(EnumType.STRING)` | O | ORDINAL 금지 |
| `@ManyToOne(fetch = LAZY)` | O | EAGER 금지 |

---

### DTO (record 기반)

#### Request DTO
```java
public class MemberRequest {

    public record Create(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        String nickname,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
    ) {}

    public record Update(
        @Size(min = 2, max = 20)
        String nickname
    ) {}
}
```

#### Response DTO
```java
public class MemberResponse {

    @Builder
    public record Detail(
        Long id,
        String nickname,
        String email,
        LocalDateTime createdAt
    ) {
        public static Detail from(Member member) {
            return Detail.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .createdAt(member.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Summary(
        Long id,
        String nickname
    ) {
        public static Summary from(Member member) {
            return Summary.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .build();
        }
    }
}
```

| 구분 | 패턴 | 비고 |
|------|------|------|
| Request | `record` + Validation | 불변, Jackson 자동 지원 |
| Response | `@Builder` + `record` | `from()` 팩토리 메서드 |

> **record 장점:** 불변성 보장, `equals/hashCode/toString` 자동 생성, 멀티스레드 안전

---

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

    @Operation(summary = "회원 생성")
    @PostMapping
    public ApiResponse<MemberResponse.Detail> createMember(
            @Valid @RequestBody MemberRequest.Create request
    ) {
        return ApiResponse.success(
                SuccessCode.MEMBER_CREATED,
                memberService.createMember(request)
        );
    }
}
```

| 어노테이션 | 순서 | 비고 |
|-----------|------|------|
| `@Tag` | 1 | Swagger 문서화 |
| `@RestController` | 2 | |
| `@RequestMapping` | 3 | |
| `@RequiredArgsConstructor` | 4 | |
| `@Operation` | 메서드 1 | summary 필수 |
| `@GetMapping` 등 | 메서드 2 | |

---

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

    @Transactional  // 쓰기 작업만
    public MemberResponse.Detail createMember(MemberRequest.Create request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .nickname(request.nickname())
                .email(request.email())
                .build();

        memberRepository.save(member);
        return MemberResponse.Detail.from(member);
    }
}
```

| 어노테이션 | 위치 | 비고 |
|-----------|------|------|
| `@Slf4j` | 클래스 | 로깅 시 |
| `@Service` | 클래스 | 필수 |
| `@RequiredArgsConstructor` | 클래스 | 필수 |
| `@Transactional(readOnly = true)` | 클래스 | 기본값 |
| `@Transactional` | 쓰기 메서드 | CUD만 |

---

### Repository

#### JPA Repository
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m JOIN FETCH m.store WHERE m.id = :id")
    Optional<Member> findByIdWithStore(@Param("id") Long id);

    @EntityGraph(attributePaths = {"store"})
    List<Member> findAllByStatus(MemberStatus status);
}
```

#### QueryDSL Repository
```java
@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Member> searchMembers(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .where(
                        nicknameContains(condition.nickname()),
                        statusEq(condition.status())
                )
                .orderBy(member.createdAt.desc())
                .fetch();
    }

    private BooleanExpression nicknameContains(String nickname) {
        return hasText(nickname) ? member.nickname.contains(nickname) : null;
    }

    private BooleanExpression statusEq(MemberStatus status) {
        return status != null ? member.status.eq(status) : null;
    }
}
```

---

## 금지 사항

```java
// ❌ 클래스 레벨 @Builder
@Entity
@Builder
@AllArgsConstructor
public class Member { }

// ❌ @Setter, @Data
@Setter
@Data
public class Member { }

// ❌ EAGER (기본값)
@ManyToOne
private Store store;

// ❌ ORDINAL (기본값)
@Enumerated
private MemberStatus status;

// ❌ Controller에서 예외 처리
@GetMapping("/{id}")
public ApiResponse<?> get(@PathVariable Long id) {
    try { ... } catch (Exception e) { ... }
}
```

---

## Validation 규칙

| 검증 유형 | 위치 | 예시 |
|----------|------|------|
| 형식 검증 | DTO | `@NotBlank`, `@Email`, `@Size` |
| 비즈니스 검증 | Service | 중복 체크, 권한 검사 |

```java
// Service에서 비즈니스 검증
if (memberRepository.existsByEmail(request.email())) {
    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
}
```

---

## Null & Optional 처리

```java
// ✅ 단건: orElseThrow 즉시 호출
Member member = memberRepository.findById(id)
    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

// ✅ 목록: 빈 리스트 반환
public List<Member> getMembers() {
    return memberRepository.findAll();
}

// ❌ Optional 파라미터/필드 금지
void doSomething(Optional<String> name);
private Optional<String> nickname;
```

---

## 테스트 코드

### 네이밍
```java
class MemberServiceTest { }

@Test
void 회원_조회_성공() { }

@Test
void 존재하지_않는_회원_조회시_예외발생() { }
```

### FixtureMonkey 활용
```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Test
    void 회원_조회_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));

        // when
        MemberResponse.Detail result = memberService.getMember(1L);

        // then
        assertThat(result.id()).isEqualTo(member.getId());
    }
}
```

> **FixtureMonkey 장점:** 랜덤 테스트 데이터 자동 생성, 엣지 케이스 발견 용이

### Given-When-Then 패턴
```java
@Test
void 회원_생성_성공() {
    // given - 테스트 데이터 준비
    MemberRequest.Create request = new MemberRequest.Create("닉네임", "test@test.com");

    // when - 테스트 대상 실행
    MemberResponse.Detail result = memberService.createMember(request);

    // then - 결과 검증
    assertThat(result.nickname()).isEqualTo("닉네임");
}
```

---

## 날짜 & 시간

```java
// ✅ LocalDateTime 사용
private LocalDateTime createdAt;

// ❌ Date, Timestamp 금지
private Date createdAt;
```

### API 응답 포맷
```java
// ISO 8601 (JacksonConfig 전역 설정)
"createdAt": "2025-01-15T14:30:00"

// 특정 포맷 필요시
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createdAt;
```

---

## 상수 처리

```java
// ❌ 매직 넘버
if (retryCount > 3) { }

// ✅ 상수 또는 Enum
private static final int MAX_RETRY_COUNT = 3;
if (retryCount > MAX_RETRY_COUNT) { }
```

---

## 로깅

```java
log.debug()  // 개발 디버깅용
log.info()   // 비즈니스 로직 흐름
log.warn()   // 예상된 예외
log.error()  // 시스템 에러
```

### 패턴
```java
log.info("[MemberService.getMember] memberId: {}", memberId);
```

### 민감정보 금지
```java
// ❌ 금지
log.info("회원: {}", member);      // 전체 객체
log.info("비밀번호: {}", password);
log.info("토큰: {}", accessToken);
```

---

## 메서드 파라미터

```java
// ❌ 파라미터 3개 초과
void create(Long a, Long b, LocalDateTime c, String d, int e);

// ✅ Command 객체로 묶기
void create(ReservationCommand command);

public record ReservationCommand(
    Long memberId,
    Long storeId,
    LocalDateTime date,
    String memo
) {}
```
