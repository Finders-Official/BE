package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_dtype", columnList = "dtype")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 읽기 전용
    @Column(name = "dtype", nullable = false, insertable = false, updatable = false, length = 20)
    private String dtype;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "refresh_token_hash", length = 500)
    private String refreshTokenHash;
}
