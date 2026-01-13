package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.enums.MemberType;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_role", columnList = "role")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 읽기 전용
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, insertable = false, updatable = false, length = 20)
    private MemberType role;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "refresh_token_hash", length = 500)
    private String refreshTokenHash;

    protected Member(String name, String email, String phone, MemberType role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = MemberStatus.ACTIVE;
        this.role = role;
    }

    public void updateRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }
}
