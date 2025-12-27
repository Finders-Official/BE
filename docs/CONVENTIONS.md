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

#### 브랜치 구조
```
main                        # 운영 브랜치
develop                     # 개발 브랜치
<type>/<설명>-#<issue>       # 작업 브랜치
```

#### 형식
```
<type>/<간단한_설명>-#<issue_number>
```

| 구성요소 | 설명 | 예시 |
|----------|------|------|
| `type` | 작업 유형 | `feat`, `fix`, `refactor`, `hotfix` |
| `설명` | 간단한 작업 설명 (영문, 케밥케이스) | `signup-api`, `image-upload` |
| `issue_number` | GitHub 이슈 번호 **(필수)** | `#14`, `#23`, `#108` |

#### 예시
```bash
feat/signup-api-#14          # 회원가입 API 기능 추가
fix/image-upload-#23         # 이미지 업로드 버그 수정
refactor/token-logic-#8      # 토큰 로직 리팩토링
hotfix/date-bug-#45          # 날짜 관련 긴급 버그 수정
chore/docker-setup-#5        # Docker 환경 설정
```

---

### Commit Message

#### 형식
```
<type>: <subject> (#<issue_number>)

<body>
```

#### 커밋 유형 (Type)

| Type | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드 추가/수정 |
| `chore` | 빌드 설정, 패키지 매니저, 코드 포맷팅 등 |
| `rename` | 파일/폴더 이름 변경 또는 이동 |
| `remove` | 파일 삭제 |

#### 규칙
1. **type**: 소문자 영문
2. **subject**: 한글 또는 영문, 50자 이내, 마침표 없음
3. **body**: 한글 작성 권장, 무엇을 왜 변경했는지 설명
4. **이슈 번호**: 선택 사항 (참고용)

#### 예시

```bash
# 간단한 커밋
git commit -m "feat: 회원가입 API 구현 (#14)"

# 본문 포함 커밋
git commit -m "feat: 소셜 로그인 기능 추가 (#28)

- 카카오 OAuth2 로그인 구현
- 애플 OAuth2 로그인 구현
- 소셜 계정 연동 테이블 추가"

# 버그 수정
git commit -m "fix: 이미지 업로드 시 NPE 발생 수정 (#45)"

# 리팩토링
git commit -m "refactor: 예약 검증 로직 분리 (#67)"

# 이슈 번호 없이 (선택 사항)
git commit -m "chore: 코드 포맷팅 적용"
```

---

### Issue & PR 연동

- 브랜치 생성 시 반드시 이슈 먼저 생성
- 브랜치명에 이슈 번호 필수 포함
- 커밋 메시지 이슈 번호는 선택 (참고용)
- PR 머지 시 이슈 자동 종료: `Closes #14`, `Fixes #14`
