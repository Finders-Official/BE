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

    // 필드 3개 이하 → 생성자 직접 사용
    public record Summary(
        Long id,
        String nickname
    ) {
        public static Summary from(Member member) {
            return new Summary(member.getId(), member.getNickname());
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

### Entity → DTO 변환 패턴 (Converter)

#### 개요

프로젝트에서는 **별도의 Converter 클래스를 만들지 않고**, DTO 내부에 `from()` 정적 팩토리 메서드를 사용하여 Entity를 DTO로 변환합니다.

#### 패턴 설명

```java
public class PaymentResponse {

    @Builder
    public record Detail(
        Long id,
        String paymentId,
        String orderName,
        Integer amount,
        PaymentStatus status
        // ... 기타 필드
    ) {
        // ✅ 이것이 컨버터 역할을 합니다
        public static Detail from(Payment payment) {
            return Detail.builder()
                    .id(payment.getId())
                    .paymentId(payment.getPaymentId())
                    .orderName(payment.getOrderName())
                    .amount(payment.getAmount())
                    .status(payment.getStatus())
                    .build();
        }
    }

    public record Summary(
        Long id,
        String paymentId,
        String orderName
    ) {
        // 필드가 적으면 Builder 없이 생성자 사용
        public static Summary from(Payment payment) {
            return new Summary(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrderName()
            );
        }
    }
}
```

#### 사용 예시

```java
// Service에서 사용
@Override
public PaymentResponse.Detail getPayment(Long memberId, String paymentId) {
    Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

    // ✅ 간결한 변환
    return PaymentResponse.Detail.from(payment);
}

// 리스트 변환
@Override
public List<PaymentResponse.Summary> getMyPayments(Long memberId) {
    return paymentRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(PaymentResponse.Summary::from)  // ✅ 메서드 레퍼런스 사용
            .toList();
}
```

#### 이 패턴의 장점

| 장점 | 설명 |
|------|------|
| **캡슐화** | 변환 로직이 DTO 내부에 응집됨 |
| **타입 안전성** | 컴파일 타임에 타입 체크 |
| **간결성** | Service 코드가 깔끔해짐 |
| **일관성** | 프로젝트 전체에서 동일한 패턴 사용 |
| **테스트 용이성** | DTO 단위로 변환 로직 테스트 가능 |

#### 별도 Converter 클래스를 만들지 않는 이유

```java
// ❌ 이런 클래스를 만들지 않습니다
@Component
public class PaymentConverter {
    public PaymentResponse.Detail toDetail(Payment payment) {
        return PaymentResponse.Detail.builder()
                .id(payment.getId())
                // ...
                .build();
    }
}
```

**이유:**
1. **불필요한 추상화**: DTO 변환은 단순한 매핑 작업
2. **파일 증가**: 도메인당 Converter 클래스 추가 시 파일 수 2배 증가
3. **의존성 주입 불필요**: 정적 메서드로 충분
4. **응집도 저하**: 변환 로직이 DTO에서 분리되면 관리 포인트 증가

#### 프로젝트 적용 현황

현재 **18개 도메인, 42개의 `from()` 메서드**가 이 패턴을 따르고 있습니다:

- ✅ payment (Detail, Summary)
- ✅ community (PostResponse, CommentResponse)
- ✅ photo (PhotoResponse, RestorationResponse)
- ✅ store (PhotoLabResponse)
- ✅ inquiry (InquiryResponse)
- ✅ member (MemberResponse)
- ✅ auth (AuthResponse)
- ✅ reservation (ReservationResponse)

#### 복잡한 변환이 필요한 경우

외부 데이터나 추가 로직이 필요한 경우, 파라미터를 추가합니다:

```java
public record PostDetailResDTO(
    Long id,
    String content,
    boolean isLiked,
    boolean isMine,
    String profileImageUrl,
    List<PostImageResDTO> images
) {
    // ✅ 추가 정보를 파라미터로 받음
    public static PostDetailResDTO from(
        Post post,
        boolean isLiked,
        boolean isMine,
        String profileImageUrl,
        List<PostImageResDTO> images
    ) {
        return new PostDetailResDTO(
            post.getId(),
            post.getContent(),
            isLiked,
            isMine,
            profileImageUrl,
            images
        );
    }
}
```

---

### Controller

> Controller는 Query/Command Service를 모두 주입받아 사용합니다.

```java
@Tag(name = "Member", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @Operation(summary = "회원 조회")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse.Detail> getMember(
            @PathVariable Long memberId
    ) {
        return ApiResponse.success(
                SuccessCode.MEMBER_FOUND,
                memberQueryService.getMember(memberId)
        );
    }

    @Operation(summary = "회원 생성")
    @PostMapping
    public ApiResponse<Long> createMember(
            @Valid @RequestBody MemberRequest.Create request
    ) {
        return ApiResponse.success(
                SuccessCode.MEMBER_CREATED,
                memberCommandService.createMember(request)
        );
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping("/{memberId}")
    public ApiResponse<Void> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRequest.Update request
    ) {
        memberCommandService.updateMember(memberId, request);
        return ApiResponse.success(SuccessCode.MEMBER_UPDATED, null);
    }
}
```

| 어노테이션 | 순서 | 비고 |
|-----------|------|------|
| `@Tag` | 1 | Swagger 문서화 |
| `@RestController` | 2 | |
| `@RequiredArgsConstructor` | 3 | |
| `@RequestMapping` | 4 | |
| `@Operation` | 메서드 1 | summary 필수 |
| `@GetMapping` 등 | 메서드 2 | |

---

### Service (CQRS 패턴)

> **CQRS (Command Query Responsibility Segregation)**: 조회(Query)와 명령(Command)을 분리하여 책임을 명확히 합니다.

#### 패키지 구조
```
service/
├── command/
│   ├── {Domain}CommandService.java      (interface)
│   └── {Domain}CommandServiceImpl.java  (구현체)
└── query/
    ├── {Domain}QueryService.java        (interface)
    └── {Domain}QueryServiceImpl.java    (구현체)
```

#### Interface + Implementation 패턴을 사용하는 이유

**장점:**

| 측면 | 설명 |
|------|------|
| **테스트 용이성** | Interface를 Mock으로 주입하여 단위 테스트 격리 |
| **확장성** | 캐싱, 비동기 등 다른 구현체 추가 가능 |
| **명확한 계약** | Interface로 서비스 책임 명시 |
| **일관성** | 프로젝트 전체 35개 Service가 동일 패턴 |

**단점:**
- 파일 수 증가 (Service당 2개 파일)
- 구현체가 1개뿐이면 과도한 추상화로 느껴질 수 있음

**결론:**
- 프로젝트 전체가 이미 이 패턴을 따르고 있음
- Command/Query 분리와 잘 맞음
- 테스트 작성 시 실질적인 이점 존재
- **현재 구조 유지 권장**

#### 단일 Service 클래스로 변경하지 않는 이유

```java
// ❌ 이렇게 변경하지 않습니다
service/
├── command/
│   └── PaymentCommandService.java  (interface 없이 단일 클래스)
└── query/
    └── PaymentQueryService.java    (interface 없이 단일 클래스)
```

**이유:**
1. **일관성 우선**: 35개 Service를 모두 변경하는 비용이 큼
2. **테스트 복잡도**: Mockito로 구체 클래스 Mock 생성 필요
3. **확장 가능성**: 향후 다른 구현체 추가 시 리팩토링 비용 발생

**참고:**
- 소규모 프로젝트에서는 단일 클래스가 더 실용적일 수 있음
- 하지만 현재 프로젝트 규모와 팀 컨벤션을 고려하여 현재 구조 유지

#### Query Service (조회 전용)

```java
// Interface
public interface MemberQueryService {
    MemberResponse.Detail getMember(Long memberId);
    List<MemberResponse.Summary> getMembers();
}

// Implementation
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;

    @Override
    public MemberResponse.Detail getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.Detail.from(member);
    }

    @Override
    public List<MemberResponse.Summary> getMembers() {
        return memberQueryRepository.findAllSummary();
    }
}
```

#### Command Service (CUD 전용)

```java
// Interface
public interface MemberCommandService {
    Long createMember(MemberRequest.Create request);
    void updateMember(Long memberId, MemberRequest.Update request);
    void deleteMember(Long memberId);
}

// Implementation
@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberRepository memberRepository;

    @Override
    public Long createMember(MemberRequest.Create request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .nickname(request.nickname())
                .email(request.email())
                .build();

        return memberRepository.save(member).getId();
    }

    @Override
    public void updateMember(Long memberId, MemberRequest.Update request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(request.nickname());
    }

    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.softDelete();
    }
}
```

#### Service 어노테이션 정리

| 구분 | 클래스 레벨 트랜잭션 | 용도 |
|------|---------------------|------|
| QueryServiceImpl | `@Transactional(readOnly = true)` | 조회 전용 (Read) |
| CommandServiceImpl | `@Transactional` | 생성/수정/삭제 (CUD) |

| 어노테이션 | 위치 | 비고 |
|-----------|------|------|
| `@Service` | 구현체 클래스 | 필수 |
| `@RequiredArgsConstructor` | 구현체 클래스 | 필수 |
| `@Transactional(readOnly = true)` | QueryServiceImpl 클래스 | 조회 최적화 |
| `@Transactional` | CommandServiceImpl 클래스 | CUD 작업 |

> **참고:** 기존 도메인(member, auth, store 등)은 점진적으로 CQRS 패턴으로 마이그레이션 예정이며, 신규 도메인은 새 패턴을 따릅니다.

---

### Repository

> **JPA Repository**는 `interface`로, **QueryDSL Repository**는 `class`로 작성합니다.

| 구분 | 타입 | 네이밍 | 용도 |
|------|------|--------|------|
| JPA Repository | `interface` | `{Domain}Repository` | 기본 CRUD, 단순 쿼리 |
| QueryDSL Repository | `class` | `{Domain}QueryRepository` | 복잡한 동적 쿼리 |

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
