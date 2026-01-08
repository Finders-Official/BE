package com.finders.api.domain.member.repository;

import com.finders.api.domain.member.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberUserRepository extends JpaRepository<MemberUser, Long> {
}
