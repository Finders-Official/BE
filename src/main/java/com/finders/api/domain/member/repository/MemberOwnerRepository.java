package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberOwner;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MemberOwner Repository
 */
public interface MemberOwnerRepository extends JpaRepository<MemberOwner, Long> {
}
