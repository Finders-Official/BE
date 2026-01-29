package com.finders.api.domain.member.service.command;

import com.finders.api.domain.auth.dto.AuthRequest;
import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.dto.VerifiedPhoneInfo;
import com.finders.api.domain.member.dto.request.MemberPhoneRequest;
import com.finders.api.domain.member.dto.request.MemberRequest;
import com.finders.api.domain.member.dto.response.MemberPhoneResponse;
import com.finders.api.domain.member.dto.VerificationData;
import com.finders.api.domain.member.dto.response.MemberResponse;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.SocialAccount;
import com.finders.api.domain.member.repository.*;
import com.finders.api.domain.terms.entity.MemberAgreement;
import com.finders.api.domain.terms.repository.MemberAgreementRepository;
import com.finders.api.domain.terms.repository.TermsRepository;
import com.finders.api.global.config.RedisConfig;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.security.JwtTokenProvider;
import com.finders.api.global.security.RefreshTokenHasher;
import com.finders.api.infra.messaging.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCommandServiceImpl implements MemberCommandService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final MemberUserRepository memberUserRepository;
    private final MemberOwnerRepository memberOwnerRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final TermsRepository termsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final MessageService messageService;

    private static final java.security.SecureRandom random = new java.security.SecureRandom();

    private static final String PHONE_CODE_KEY = "auth:phone:code:";
    private static final String VERIFIED_PHONE_KEY = "auth:phone:verified:";

    // 휴대폰 인증번호 요청
    @Override
    public MemberPhoneResponse.SentInfo sendPhoneVerificationCode(MemberPhoneRequest.SendCode request) {

        String requestId = UUID.randomUUID().toString();
        String code = String.valueOf(random.nextInt(900000) + 100000); // 6자리 랜덤 번호

        // 유효 시간 3분 설정
        VerificationData data = new VerificationData(request.phone(), code, LocalDateTime.now().plusMinutes(RedisConfig.AUTH_CODE_TTL_MINUTES));

        redisTemplate.opsForValue().set(PHONE_CODE_KEY + requestId, data, Duration.ofMinutes(RedisConfig.AUTH_CODE_TTL_MINUTES));

        // 알림톡 발송 요청 (Console or Sendon)
        messageService.sendVerificationCode(request.phone(), code);

        return new MemberPhoneResponse.SentInfo(requestId, (int) (RedisConfig.AUTH_CODE_TTL_MINUTES * 60));
    }

    // 휴대폰 인증번호 확인
    @Override
    public MemberPhoneResponse.VerificationResult verifyPhoneCode(MemberPhoneRequest.VerifyCode request, boolean isSignupFlow) {
        String codeKey = PHONE_CODE_KEY + request.requestId();

        VerificationData data = (VerificationData) redisTemplate.opsForValue().get(codeKey);

        if (data == null || data.isExpired()) {
            throw new CustomException(ErrorCode.AUTH_PHONE_CODE_EXPIRED);
        }

        if (!data.getCode().equals(request.code())) {
            throw new CustomException(ErrorCode.AUTH_PHONE_CODE_MISMATCH);
        }

        // 인증 성공 시 인증번호 삭제
        redisTemplate.delete(codeKey);

        // 증빙 토큰 생성 및 저장
        String verifiedPhoneToken = "vpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        VerifiedPhoneInfo info = new VerifiedPhoneInfo(data.getPhone(), LocalDateTime.now().plusMinutes(RedisConfig.VERIFIED_PHONE_TTL_MINUTES));

        redisTemplate.opsForValue().set(VERIFIED_PHONE_KEY + verifiedPhoneToken, info, Duration.ofMinutes(RedisConfig.VERIFIED_PHONE_TTL_MINUTES));

        int ttlSeconds = (int) (RedisConfig.VERIFIED_PHONE_TTL_MINUTES * 60);

        if (isSignupFlow) {
            return MemberPhoneResponse.VerificationResult.signup(verifiedPhoneToken, data.getPhone(), ttlSeconds);
        } else {
            return MemberPhoneResponse.VerificationResult.myPage(verifiedPhoneToken, data.getPhone(), ttlSeconds);
        }
    }

    // 소셜 회원가입 완료
    @Override
    @Transactional
    public MemberResponse.SignupResult signupSocialComplete(SignupTokenPayload payload, MemberRequest.SocialSignupComplete request) {
        // 휴대폰 인증 증빙 확인
        if (request.verifiedPhoneToken() == null) {
            throw new CustomException(ErrorCode.MEMBER_PHONE_VERIFY_REQUIRED);
        }
        validateVPT(request.phone(), request.verifiedPhoneToken());

        // 닉네임 중복 체크
        if (memberUserRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        // 필수 약관 체크
        checkMandatoryAgreements(request.agreements());

        // MemberUser 엔티티 생성 및 저장
        MemberUser memberUser = MemberUser.builder()
                .name(payload.name())
                .email(payload.email())
                .phone(request.phone())
                .profileImage(payload.profileImage())
                .nickname(request.nickname())
                .build();

        MemberUser savedUser = memberUserRepository.save(memberUser);

        // SocialAccount 연동 정보 저장
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(payload.provider())
                .providerId(payload.providerId())
                .user(savedUser)
                .socialEmail(payload.email())
                .build();

        socialAccountRepository.save(socialAccount);

        // 약관 동의 내역 저장
        List<MemberAgreement> agreements = request.agreements().stream()
                .map(req -> MemberAgreement.builder()
                        .member(memberUser)
                        .terms(termsRepository.getReferenceById(req.termsId()))
                        .isAgreed(req.isAgreed())
                        .agreedAt(LocalDateTime.now())
                        .build())
                .toList();
        memberAgreementRepository.saveAll(agreements);

        // 정식 서비스 이용을 위한 Access/Refresh Token 발급
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId());

        refreshTokenHasher.saveRefreshToken(savedUser.getId(), refreshToken);

        redisTemplate.delete(VERIFIED_PHONE_KEY + request.verifiedPhoneToken());

        return new MemberResponse.SignupResult(
                accessToken,
                refreshToken,
                new MemberResponse.MemberSummary(memberUser.getId(), memberUser.getNickname())
        );
    }

    @Override
    @Transactional
    public Member updateProfile(Long memberId, MemberRequest.UpdateProfile request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member realMember = (Member) Hibernate.unproxy(member);

        // 전화번호 수정
        if (request.phone() != null) {
            // 전화번호 변경 시 토큰 검증
            if (request.verifiedPhoneToken() == null) {
                throw new CustomException(ErrorCode.MEMBER_PHONE_VERIFY_REQUIRED);
            }
            validateVPT(request.phone(), request.verifiedPhoneToken());
            realMember.updatePhone(request.phone());
        }

        if (realMember instanceof MemberUser memberUser) {
            // 닉네임 수정
            if (request.nickname() != null) {
                if (!request.nickname().equals(memberUser.getNickname()) &&
                    memberUserRepository.existsByNickname(request.nickname())) {
                    throw new CustomException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
                }
                memberUser.updateNickname(request.nickname());
            }

            // 프로필 이미지 수정
            if (request.profileImageUrl() != null) {
                memberUser.updateProfileImage(request.profileImageUrl());
            }
        }

        return realMember;
    }

    @Override
    public MemberOwner saveMemberOwner(AuthRequest.OwnerSignupRequest request, String encodedPassword) {
        MemberOwner owner = MemberOwner.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(encodedPassword)
                .businessNumber(request.businessNumber())
                .bankName(request.bankName())
                .bankAccountNumber(request.bankAccountNumber())
                .bankAccountHolder(request.bankAccountHolder())
                .build();

        return memberOwnerRepository.save(owner);
    }

    private void validateVPT(String phone, String token) {
        VerifiedPhoneInfo info = (VerifiedPhoneInfo) redisTemplate.opsForValue().get(VERIFIED_PHONE_KEY + token);

        if (info == null || info.isExpired()) {
            if (info != null) {
                redisTemplate.delete(VERIFIED_PHONE_KEY + token);
            }
            throw new CustomException(ErrorCode.MEMBER_PHONE_VERIFY_FAILED);
        }

        String cleanStoredPhone = info.getPhone().replaceAll("[^0-9]", "");
        String cleanRequestedPhone = phone.replaceAll("[^0-9]", "");

        if (!cleanStoredPhone.equals(cleanRequestedPhone)) {
            throw new CustomException(ErrorCode.MEMBER_PHONE_VERIFY_FAILED);
        }
    }

    private void checkMandatoryAgreements(List<MemberRequest.AgreementRequest> agreements) {
        // 실제 운영 시에는 DB나 환경설정에서 필수 약관 ID 리스트를 가져와야 함
        List<Long> mandatoryTermsIds = List.of(1L, 2L); // 예시: 1번, 2번이 필수 약관

        boolean allMandatoryAgreed = mandatoryTermsIds.stream()
                .allMatch(id -> agreements.stream()
                        .anyMatch(a -> a.termsId().equals(id) && a.isAgreed()));

        if (!allMandatoryAgreed) {
            throw new CustomException(ErrorCode.MEMBER_MANDATORY_TERMS_NOT_AGREED);
        }
    }
}
