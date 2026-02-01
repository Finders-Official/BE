package com.finders.api.domain.member.service.command;

import com.finders.api.domain.inquiry.enums.InquiryStatus;
import com.finders.api.domain.inquiry.repository.InquiryRepository;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.entity.SocialAccount;
import com.finders.api.domain.member.repository.SocialAccountRepository;
import com.finders.api.domain.member.service.query.MemberUserQueryService;
import com.finders.api.domain.photo.enums.DeliveryStatus;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.enums.PrintOrderStatus;
import com.finders.api.domain.photo.repository.DeliveryRepository;
import com.finders.api.domain.photo.repository.DevelopmentOrderRepository;
import com.finders.api.domain.photo.repository.PrintOrderRepository;
import com.finders.api.domain.reservation.enums.ReservationStatus;
import com.finders.api.domain.reservation.repository.ReservationRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.oauth.OAuthUnlinkClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberUserCommandServiceImpl implements MemberUserCommandService {

    private final MemberUserQueryService memberUserQueryService;
    private final SocialAccountRepository socialAccountRepository;
    private final ReservationRepository reservationRepository;
    private final DevelopmentOrderRepository developmentOrderRepository;
    private final PrintOrderRepository printOrderRepository;
    private final DeliveryRepository deliveryRepository;
    private final InquiryRepository inquiryRepository;

    private final List<OAuthUnlinkClient> unlinkClients;

    @Transactional
    @Override
    public void withdraw(Long memberId) {
        MemberUser memberUser = memberUserQueryService.getActiveMember(memberId);

        // 탈퇴 가능한 상태인지 확인
        validateWithdrawalEligibility(memberId);

        // 소셜 연동 해제
        List<SocialAccount> socialAccounts = socialAccountRepository.findAllByUser(memberUser);

        if (!socialAccounts.isEmpty()) {
            for (SocialAccount socialAccount : socialAccounts) {
                try {
                    unlinkSocialAccount(socialAccount);
                } catch (Exception e) {
                    log.error("[MemberUserCommandServiceImpl.withdraw] 소셜 연동 해제 실패 - 회원: {}, 계정: {}, 사유: {}",
                            memberId, socialAccount.getProvider(), e.getMessage());
                }
                socialAccount.softDelete();
            }
        }

        // 메인 계정 상태 변경 (Soft Delete)
        memberUser.withdraw();

        log.info("[MemberUserCommandServiceImpl.withdraw] 회원 탈퇴 처리 완료(15일 유예 기간 시작): memberId={}", memberId);
    }

    private void validateWithdrawalEligibility(Long memberId) {
        // 진행 중인 예약이 있는지 확인
        boolean hasActiveReservation = reservationRepository.existsByUserIdAndStatusIn(
                memberId, List.of(ReservationStatus.RESERVED)
        );

        // 진행 중인 현상/스캔이 있는지 확인
        boolean hasActiveDeveloping = developmentOrderRepository.existsByUserIdAndStatusNot(
                memberId, DevelopmentOrderStatus.COMPLETED
        );

        // 진행 중인 인화가 있는지 확인
        boolean hasActivePrintOrder = printOrderRepository.existsByUserIdAndStatusNot(
                memberId, PrintOrderStatus.COMPLETED
        );

        // 진행 중인 배달이 있는지 확인
        boolean hasActiveDelivery = deliveryRepository.existsByPrintOrderUserIdAndStatusNot(
                memberId, DeliveryStatus.DELIVERED
        );

        // 진행 중인 문의가 있는지 확인
        boolean hasActiveInquiry = inquiryRepository.existsByMemberIdAndStatusIn(
                memberId, List.of(InquiryStatus.PENDING)
        );

        if (hasActiveReservation || hasActiveDeveloping || hasActivePrintOrder || hasActiveDelivery || hasActiveInquiry) {
            throw new CustomException(ErrorCode.MEMBER_WITHDRAWAL_LOCKED);
        }
    }

    private void unlinkSocialAccount(SocialAccount socialAccount) {
        OAuthUnlinkClient client = unlinkClients.stream()
                .filter(c -> c.getProvider() == socialAccount.getProvider())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER));

        client.unlink(socialAccount.getProviderId());
    }
}
