package com.finders.api.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("ADMIN")
@PrimaryKeyJoinColumn(name = "member_id")
public class MemberAdmin extends Member {

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
