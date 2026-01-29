package com.finders.api.domain.member.scheduler;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.member.repository.SocialAccountRepository;
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
    private final SocialAccountRepository socialAccountRepository;

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
