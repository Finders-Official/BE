package com.finders.api.domain.terms.repository;

import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.domain.terms.entity.TermsSocialMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TermsSocialMappingRepository extends JpaRepository<TermsSocialMapping, Long> {

    // 특정 소셜 업체와 태그 리스트에 해당하는 매핑 정보 조회
    @Query("select tsm from TermsSocialMapping tsm " +
            "join fetch tsm.terms t " +
            "where tsm.provider = :provider " +
            "and tsm.socialTag in :socialTags " +
            "and t.isActive = true")
    List<TermsSocialMapping> findAllActiveByProviderAndSocialTagIn(
            @Param("provider") SocialProvider provider,
            @Param("socialTags") List<String> socialTags
    );
}
