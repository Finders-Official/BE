package com.finders.api.domain.terms.repository;

import com.finders.api.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
