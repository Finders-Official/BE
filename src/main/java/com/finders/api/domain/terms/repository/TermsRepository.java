package com.finders.api.domain.terms.repository;

import com.finders.api.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    // 현재 활성화된 필수 약관 목록 조회
    List<Terms> findAllByIsRequiredTrueAndIsActiveTrue();
}
