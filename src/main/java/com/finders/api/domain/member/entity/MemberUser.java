package com.finders.api.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_user",
        indexes = {
                @Index(name = "uk_member_user_nickname", columnList = "nickname", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("USER")
@PrimaryKeyJoinColumn(name = "member_id")
public class MemberUser extends Member {

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "token_balance", nullable = false)
    private Integer tokenBalance;

    @Column(name = "last_token_refresh_at")
    private LocalDateTime lastTokenRefreshAt;

    @Builder
    private MemberUser(
            String name,
            String email,
            String phone,
            String profileImage,
            String nickname
    ) {
        super(name, email, phone, profileImage);
        this.nickname = nickname;
        this.tokenBalance = 3;
        this.lastTokenRefreshAt = null;
    }
}
