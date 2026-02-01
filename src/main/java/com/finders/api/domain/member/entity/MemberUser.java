package com.finders.api.domain.member.entity;

import com.finders.api.domain.member.enums.MemberStatus;
import com.finders.api.domain.member.enums.MemberType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

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
        super(name, email, phone, MemberType.USER);
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.tokenBalance = 3;
        this.lastTokenRefreshAt = null;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // === 토큰 비즈니스 메서드 ===

    /**
     * 토큰 잔액이 충분한지 확인
     */
    public boolean hasEnoughTokens(int amount) {
        return this.tokenBalance >= amount;
    }

    /**
     * 토큰 차감
     *
     * @param amount 차감할 토큰 수량
     * @return 차감 후 잔액
     * @throws IllegalStateException 잔액 부족 시
     */
    public int deductTokens(int amount) {
        if (!hasEnoughTokens(amount)) {
            throw new IllegalStateException("토큰 잔액이 부족합니다. 현재: " + tokenBalance + ", 필요: " + amount);
        }
        this.tokenBalance -= amount;
        return this.tokenBalance;
    }

    /**
     * 토큰 추가 (환불, 충전 등)
     *
     * @param amount 추가할 토큰 수량
     * @return 추가 후 잔액
     */
    public int addTokens(int amount) {
        this.tokenBalance += amount;
        return this.tokenBalance;
    }

    public void withdraw() {
        super.deactivate();
        this.nickname = "withdrawn_" + UUID.randomUUID().toString();
    }

    // 탈퇴 후 실행될 개인정보 익명화
    @Override
    public void anonymize() {
        super.anonymize();
        this.profileImage = null;
        this.tokenBalance = 0;
        this.lastTokenRefreshAt = null;
    }

    // 상태가 탈퇴인 경우 무조건 "알 수 없음"을 반환
    public String getNickname() {
        if (this.getStatus() == MemberStatus.WITHDRAWN) {
            return "알 수 없음";
        }
        return this.nickname;
    }
}
