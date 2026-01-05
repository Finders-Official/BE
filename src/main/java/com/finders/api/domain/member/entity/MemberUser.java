package com.finders.api.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
    private Integer tokenBalance = 3;

    @Column(name = "last_token_refresh_at")
    private LocalDateTime lastTokenRefreshAt;
}
