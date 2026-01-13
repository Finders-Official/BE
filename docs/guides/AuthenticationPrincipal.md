## 🔐 커스텀 인증 객체(AuthUser) 사용 가이드

보안 계층의 독립성과 성능 최적화를 위해, 세션에 JPA 엔티티(Member 등)를 직접 담지 않고 **가벼운 DTO 객체인 AuthUser**를 사용합니다.

이 문서는 컨트롤러와 서비스 레이어에서 인증 정보를 안전하고 편리하게 사용하는 방법을 설명합니다.

### 1. AuthUser 구조
AuthUser는 UserDetails를 구현한 Java Record 객체로, 토큰 검증 시점에 생성되어 SecurityContext에 저장됩니다.
- memberId: 회원의 고유 식별자 (Long)
- role: 회원의 권한 (String, 예: "USER", "GUEST", "OWNER")
- authorities: Spring Security 권한 목록

### 2. 컨트롤러에서 사용 방법

컨트롤러 메서드의 파라미터에서 @AuthenticationPrincipal 어노테이션을 사용하여 현재 로그인한 유저 정보를 가져옵니다.

#### ✅ 기본 사용법
``` Java
@GetMapping("/api/v1/members/me")
public ApiResponse<MemberResponse.MyProfile> getMyProfile(
    @AuthenticationPrincipal AuthUser authUser // 인증 객체 주입
) {
    // memberId를 사용하여 서비스 호출
    return ApiResponse.success(SuccessCode.MEMBER_INFO_FOUND,
                    memberQueryService.getMyProfile(authUser.memberId()));
}
```
]
#### ⚠️ 주의사항
- 타입 명시: 반드시 AuthUser 타입을 명시해야 합니다. 
- Null 체크: SecurityConfig에서 permitAll()로 설정된 공공 API의 경우, 비로그인 상태라면 authUser는 null이 됩니다. 필요한 경우 null 체크 로직을 넣으세요.

### 3. 권한 제어 (Role-Based Access Control)

1) URL 기반 제어 (SecurityConfig)
  - 대부분의 경로는 SecurityConfig에서 이미 설정되어 있습니다. 
  - .anyRequest().hasRole("USER"): 기본적으로 모든 요청은 정식 회원(USER)만 가능합니다. 
  - 특정 경로에 대해 GUEST나 ADMIN 권한이 필요하다면 SecurityConfig를 수정하세요.

2) 메서드 기반 제어 (@PreAuthorize)
   - 메서드 단위로 더 세밀한 권한 제어가 필요할 때 사용합니다.

``` Java
@PostMapping("/api/v1/admin/notice")
@PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
public ApiResponse<?> createNotice(@AuthenticationPrincipal AuthUser authUser) {
    return ...;
}
```

### 4. 서비스 레이어와의 협업 규칙 (중요)
#### ❌ 엔티티/인증 객체를 직접 넘기지 마세요
서비스 레이어의 메서드에 AuthUser 객체나 MemberUser 엔티티를 통째로 넘기는 것은 지양합니다.

#### ✅ 유저 ID(memberId)만 넘기세요
서비스 레이어에서는 오직 **Long memberId**만 받아서 필요한 경우 직접 조회하여 사용합니다.

💡 Tip: 서비스 레이어 타입 체크 JPA 상속 구조를 사용하므로, instanceof MemberUser를 사용할 때는 Hibernate.unproxy(member)를 통해 실제 객체를 꺼낸 뒤 검증하는 것을 권장합니다.

``` Java
// Controller
memberService.updateNickname(authUser.memberId(), newNickname);

// Service
@Transactional
public void updateNickname(Long memberId, String newNickname) {
    MemberUser memberUser = memberUserRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    memberUser.updateNickname(newNickname);
}
```

### 5. 자주 묻는 질문 (FAQ)
Q1. AuthUser에서 닉네임이나 이메일을 바로 가져올 수 없나요?

A1. AuthUser는 토큰에 담긴 최소한의 정보(memberId, role)만 가집니다. 최신 상태의 닉네임이나 추가 정보가 필요하다면 서비스 레이어에서 DB를 조회하는 것이 원칙입니다. (데이터 정합성 보장)

Q2. 테스트 코드에서는 어떻게 사용하나요?
A2. @WithMockUser는 기본 User 객체를 생성하므로 우리 프로젝트의 AuthUser와 호환되지 않을 수 있습니다. 테스트를 위한 별도의 SecurityContextFactory를 사용하거나, Mocking을 통해 AuthUser를 주입해야 합니다.