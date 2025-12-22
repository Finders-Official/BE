# Code Conventions

## Naming Conventions

### Package
- 소문자만 사용
- 단수형 사용
```
com.finders.api.domain.member (O)
com.finders.api.domain.Members (X)
```

### Class
- PascalCase 사용
- 역할에 따른 접미사

| Type | Suffix | Example |
|------|--------|---------|
| Controller | `Controller` | `MemberController` |
| Service | `Service` | `MemberService` |
| Repository | `Repository` | `MemberRepository` |
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

## Code Style

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    // ========================================
    // 생성 메서드
    // ========================================
    @Builder
    private Member(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
        this.status = MemberStatus.ACTIVE;
    }

    // ========================================
    // 비즈니스 메서드
    // ========================================
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

### DTO

```java
public class MemberRequest {

    @Getter
    @NoArgsConstructor
    public static class Create {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        private String nickname;

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {
        @Size(min = 2, max = 20)
        private String nickname;
    }
}
```

```java
public class MemberResponse {

    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String nickname;
        private String email;
        private LocalDateTime createdAt;

        public static Detail from(Member member) {
            return Detail.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .createdAt(member.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Summary {
        private Long id;
        private String nickname;

        public static Summary from(Member member) {
            return Summary.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .build();
        }
    }
}
```

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

    @Operation(summary = "회원 정보 수정")
    @PatchMapping("/{memberId}")
    public ApiResponse<MemberResponse.Detail> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRequest.Update request
    ) {
        return ApiResponse.success(
                SuccessCode.MEMBER_UPDATED,
                memberService.updateMember(memberId, request)
        );
    }
}
```

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

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(request.getNickname());

        return MemberResponse.Detail.from(member);
    }
}
```

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

## Git Conventions

### Branch Naming
```
main            # 운영 브랜치
develop         # 개발 브랜치
feature/{기능명}  # 기능 개발
fix/{버그명}      # 버그 수정
hotfix/{이슈}    # 긴급 수정
```

### Commit Message
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅 (기능 변경 없음)
refactor: 코드 리팩토링
test: 테스트 코드 추가/수정
chore: 빌드 설정, 패키지 매니저 설정 등
```

### Example
```bash
git commit -m "feat: 회원 가입 API 구현"
git commit -m "fix: 토큰 만료 시 에러 처리"
git commit -m "docs: API 문서 업데이트"
```
