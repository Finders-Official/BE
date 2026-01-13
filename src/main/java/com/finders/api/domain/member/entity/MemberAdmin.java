package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.MemberType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder
    private MemberAdmin(String name, String email, String phone, String passwordHash) {
        super(name, email, phone, MemberType.ADMIN);
        this.passwordHash = passwordHash;
    }
}
