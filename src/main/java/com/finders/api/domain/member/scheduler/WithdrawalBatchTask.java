package com.finders.api.domain.member.scheduler;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.repository.MemberAddressRepository;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.SocialAccountRepository;
import com.finders.api.domain.member.repository.TokenHistoryRepository;
import com.finders.api.domain.terms.repository.MemberAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalBatchTask {

    private final MemberUserRepository memberUserRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TokenHistoryRepository tokenHistoryRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")    // 매일 새벽 3시 실행
    public void cleanupDeletedUsers() {
        // TODO: 기획에 맞춰서 날짜 변경 필요
        LocalDateTime threshold = LocalDateTime.now().minusDays(15);
        log.info("[WithdrawalBatchTask.cleanupDeletedUsers] 탈퇴 데이터 정리 시작 (기준일시: {})", threshold);

        try {
            // SocialAccount는 Hard Delete
            int deletedSocialCount = socialAccountRepository.deleteAllByDeletedAtBefore(threshold);
            log.info("[WithdrawalBatchTask.cleanupDeletedUsers] 소셜 계정 삭제 완료: {}건", deletedSocialCount);

            // MemberUser는 익명화 (Soft Delete 유지)
            List<MemberUser> membersToAnonymize = memberUserRepository
                    .findAllByStatusAndDeletedAtBefore(MemberStatus.WITHDRAWN, threshold);

            if (!membersToAnonymize.isEmpty()) {
                // 약관 동의 기록 삭제 Hard Delete
                memberAgreementRepository.deleteAllByMemberIn(membersToAnonymize);

                // 토큰 히스토리 삭제 Hard Delete
                tokenHistoryRepository.deleteAllByUserIn(membersToAnonymize);

                // 회원들의 배송지 정보 일괄 Hard Delete
                memberAddressRepository.deleteAllByMemberIn(membersToAnonymize);

                for (MemberUser memberUser : membersToAnonymize) {
                    memberUser.anonymize();
                }
                log.info("[WithdrawalBatchTask.cleanupDeletedUsers] 회원 정보 익명화 완료: {}건", membersToAnonymize.size());
            } else {
                log.info("[WithdrawalBatchTask.cleanupDeletedUsers] 익명화 대상 회원 없음");
            }
            log.info("[WithdrawalBatchTask.cleanupDeletedUsers] 탈퇴 데이터 정리 정상 종료");
        } catch (Exception e) {
            log.error("[WithdrawalBatchTask.cleanupDeletedUsers] 데이터 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
