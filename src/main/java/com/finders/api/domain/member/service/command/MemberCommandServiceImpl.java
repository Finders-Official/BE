package com.finders.api.domain.member.service.command;

import com.finders.api.domain.auth.dto.SignupTokenPayload;
import com.finders.api.domain.member.dto.VerifiedPhoneInfo;
import com.finders.api.domain.member.dto.request.MemberPhoneRequest;
import com.finders.api.domain.member.dto.request.MemberRequest;
import com.finders.api.domain.member.dto.response.MemberPhoneResponse;
import com.finders.api.domain.member.dto.VerificationData;
import com.finders.api.domain.member.dto.response.MemberResponse;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberAgreementRepository;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.terms.entity.MemberAgreement;
import com.finders.api.domain.terms.repository.TermsRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.global.security.JwtTokenProvider;
import com.finders.api.global.security.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberRepository memberRepository;
    private final MemberUserRepository memberUserRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final TermsRepository termsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    // 인증번호 대조용 저장소 (3분)
    private final Map<String, VerificationData> phoneVerificationStorage = new ConcurrentHashMap<>();
    // 인증 완료 증빙 토큰 저장소 (10분)
    private final Map<String, VerifiedPhoneInfo> verifiedTokenStorage = new ConcurrentHashMap<>();

    // 휴대폰 인증번호 요청
    @Override
    public MemberPhoneResponse.SentInfo sendPhoneVerificationCode(MemberPhoneRequest.SendCode request) {

        String requestId = UUID.randomUUID().toString();
        java.security.SecureRandom random = new java.security.SecureRandom();
        String code = String.valueOf(random.nextInt(900000) + 100000); // 6자리 랜덤 번호

        // 유효 시간 3분 설정
        VerificationData data = new VerificationData(request.phone(), code, LocalDateTime.now().plusMinutes(3));
        phoneVerificationStorage.put(requestId, data);

        log.info("[MemberCommandServiceImpl.sendPhoneVerificationCode] 인증번호 발송 대상: {}, 발급된 인증번호: {}", request.phone(), code);

        // TODO: 실제 SMS 전송

        return new MemberPhoneResponse.SentInfo(requestId, 180);
    }

    // 휴대폰 인증번호 확인
    @Override
    public MemberPhoneResponse.VerificationResult verifyPhoneCode(MemberPhoneRequest.VerifyCode request, boolean isSignupFlow) {
        VerificationData data = phoneVerificationStorage.get(request.requestId());

        if (data == null || data.isExpired()) {
            phoneVerificationStorage.remove(request.requestId());
            throw new CustomException(ErrorCode.AUTH_PHONE_CODE_EXPIRED);
        }

        if (!data.code().equals(request.code())) {
            throw new CustomException(ErrorCode.AUTH_PHONE_CODE_MISMATCH);
        }

        phoneVerificationStorage.remove(request.requestId());

        // 증빙 토큰 생성 및 저장
        String verifiedPhoneToken = "vpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        verifiedTokenStorage.put(verifiedPhoneToken,
                new VerifiedPhoneInfo(data.phone(), LocalDateTime.now().plusMinutes(10)));

        if (isSignupFlow) {
            return MemberPhoneResponse.VerificationResult.signup(verifiedPhoneToken, data.phone(), 600);
        } else {
            return MemberPhoneResponse.VerificationResult.myPage(verifiedPhoneToken, data.phone(), 600);
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

    private void validateVPT(String phone, String token) {
        VerifiedPhoneInfo info = verifiedTokenStorage.get(token);

        if (info == null || info.isExpired()) {
            if (info != null) verifiedTokenStorage.remove(token);
            throw new CustomException(ErrorCode.MEMBER_PHONE_VERIFY_FAILED);
        }

        String cleanStoredPhone = info.phone().replaceAll("[^0-9]", "");
        String cleanRequestedPhone = phone.replaceAll("[^0-9]", "");

        if (!cleanStoredPhone.equals(cleanRequestedPhone)) {
            throw new CustomException(ErrorCode.MEMBER_PHONE_VERIFY_FAILED);
        }

        verifiedTokenStorage.remove(token);
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
