package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.SocialAccount;
import com.finders.api.domain.member.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider socialProvider, String providerId);

    List<SocialAccount> findAllByUser(Member member);

     // 삭제 시점이 threshold 이전인 데이터 일괄 삭제 (성능을 위해 벌크 연산 사용)
    @Modifying
    @Query("delete from SocialAccount s where s.deletedAt <= :threshold")
    int deleteAllByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
