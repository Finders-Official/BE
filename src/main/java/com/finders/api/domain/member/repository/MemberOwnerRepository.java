package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * MemberOwner Repository
 */
public interface MemberOwnerRepository extends JpaRepository<MemberOwner, Long> {

    Optional<MemberOwner> findByEmail(String email);
}
