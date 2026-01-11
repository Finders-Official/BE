package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.SocialAccount;
import com.finders.api.domain.member.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider socialProvider, String providerId);
}
