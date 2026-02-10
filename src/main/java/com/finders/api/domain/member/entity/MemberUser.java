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

    @Column(name = "credit_balance", nullable = false)
    private Integer creditBalance;

    @Column(name = "last_credit_refresh_at")
    private LocalDateTime lastCreditRefreshAt;

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
        this.creditBalance = 3;
        this.lastCreditRefreshAt = null;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // === 크레딧 비즈니스 메서드 ===

    /**
     * 크레딧 잔액이 충분한지 확인
     */
    public boolean hasEnoughCredits(int amount) {
        return this.creditBalance >= amount;
    }

    /**
     * 크레딧 차감
     *
     * @param amount 차감할 크레딧 수량
     * @return 차감 후 잔액
     * @throws IllegalStateException 잔액 부족 시
     */
    public int deductCredits(int amount) {
        if (!hasEnoughCredits(amount)) {
            throw new IllegalStateException("크레딧 잔액이 부족합니다. 현재: " + creditBalance + ", 필요: " + amount);
        }
        this.creditBalance -= amount;
        return this.creditBalance;
    }

    /**
     * 크레딧 추가 (환불, 충전 등)
     *
     * @param amount 추가할 크레딧 수량
     * @return 추가 후 잔액
     */
    public int addCredits(int amount) {
        this.creditBalance += amount;
        return this.creditBalance;
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
        this.creditBalance = 0;
        this.lastCreditRefreshAt = null;
    }

    // 상태가 탈퇴인 경우 무조건 "알 수 없음"을 반환
    public String getNickname() {
        if (this.getStatus() == MemberStatus.WITHDRAWN) {
            return "알 수 없음";
        }
        return this.nickname;
    }
}
